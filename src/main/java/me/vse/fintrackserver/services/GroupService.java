package me.vse.fintrackserver.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.enums.UserRights;
import me.vse.fintrackserver.mappers.GroupMapper;
import me.vse.fintrackserver.model.*;
import me.vse.fintrackserver.model.dto.GroupDto;
import me.vse.fintrackserver.repositories.*;
import me.vse.fintrackserver.rest.responses.GroupViewResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserGroupRelationRepository userGroupRelationRepository;

    @Transactional
    public List<GroupViewResponse> getAll(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException(ErrorMessages.ACCOUNT_DOESNT_EXIST.name());
        }
        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new IllegalArgumentException(ErrorMessages.ACCOUNT_DOESNT_EXIST.name());
        }

        List<GroupViewResponse> groups = user.getUserGroupRelations().stream()
                .map(UserGroupRelation::getGroup)
                .filter(group -> !group.isRemoved())
                .map(group -> GroupViewResponse.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .users(
                                group.getGroupUsersRelations()
                                        .stream()
                                        .map(UserGroupRelation::getUser)
                                        .collect(Collectors.toList()))
                        .accounts(
                                group.getAccountGroupsRelations()
                                        .stream()
                                        .map(AccountGroupRelation::getAccount)
                                        .filter(account -> !account.isRemoved())
                                        .collect(Collectors.toList())
                        )
                        .build())
                .toList();

        GroupViewResponse otherGroup = GroupViewResponse.builder()
                .accounts(user.getAccountUserRights()
                        .stream()
                        .filter(rights -> rights.getRights().equals(UserRights.READ) || rights.getRights().equals(UserRights.WRITE))
                        .map(AccountUserRights::getAccount)
                        .filter(currAccount -> !currAccount.isRemoved())
                        .filter(account -> groups.stream()
                                .map(GroupViewResponse::getAccounts)
                                .flatMap(List::stream)
                                .noneMatch(existingAccount -> existingAccount.getId().equals(account.getId())))
                        .collect(Collectors.toList())
                )
                .build();

        List<GroupViewResponse> response = new ArrayList<>(groups);
        response.add(otherGroup);
        return response;
    }

    @Transactional
    public Group add(GroupDto groupDto) {
        String name = groupDto.getName();
        if (name == null) {
            throw new IllegalArgumentException(ErrorMessages.INCORRECT_GROUP_NAME.name());
        }

        User admin = entityManager.find(User.class, groupDto.getAdminId());

        if (admin == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        List<User> members = userRepository.findUsers(groupDto.getMemberIds());

        Group group = Group.builder()
                .name(groupDto.getName())
                .isRemoved(false)
                .owner(admin)
                .code("1234")
                .build();

        List<UserGroupRelation> userGroupRelations = new ArrayList<>();
        userGroupRelations.add(new UserGroupRelation(admin, group));
        userGroupRelations.addAll(members.stream()
                .map(user -> new UserGroupRelation(user, group))
                .toList());

        Group existingGroupByCode;
        do {
            String newCode = generateGroupCode(name);
            existingGroupByCode = groupRepository.findByCode(newCode);
            group.setCode(newCode);
        } while (existingGroupByCode != null);

        entityManager.persist(group);
        userGroupRelationRepository.saveAll(userGroupRelations);

        List<Account> accounts = accountRepository.findAllByIds(groupDto.getAccountIds());

        for (Account account : accounts) {
            AccountGroupRelation agr = AccountGroupRelation.builder()
                    .group(group)
                    .account(account)
                    .build();

            entityManager.persist(agr);
        }

        return group;
    }

    @Transactional
    public Group update(GroupDto groupDto) {
        String name = groupDto.getName();
        if (name == null) {
            throw new IllegalArgumentException(ErrorMessages.INCORRECT_GROUP_NAME.name());
        }

        Group group = entityManager.find(Group.class, groupDto.getId());
        if (group == null) {
            throw new IllegalArgumentException(ErrorMessages.GROUP_DOESNT_EXIST.name());
        }

        if (!group.getOwner().getId().equals(groupDto.getAdminId())) {
            throw new IllegalArgumentException(ErrorMessages.ACCOUNT_DOESNT_EXIST.name());
        }

        List<User> members = userRepository.findUsers(groupDto.getMemberIds());
        List<Account> accounts = accountRepository.findAllByIds(groupDto.getAccountIds());

        boolean doesntHaveRightsForOneOrMoreAccounts =
                !accounts.isEmpty() &&
                accounts.stream()
                        .filter(account -> !account.isRemoved())
                        .noneMatch(account -> account.getUserRights().stream()
                                .filter(accountUserRights -> accountUserRights.getUser()
                                        .getId()
                                        .equals(groupDto.getAdminId()))
                                .anyMatch(AccountUserRights::isOwner));
        if (doesntHaveRightsForOneOrMoreAccounts) {
            throw new IllegalArgumentException(ErrorMessages.UNPERMITTED_OPERATION.name());
        }

        List<UserGroupRelation> userGroupRelations = new ArrayList<>(members.stream()
                .filter(user -> group.getGroupUsersRelations()
                        .stream()
                        .noneMatch(groupUserRelation -> groupUserRelation.getGroup()
                                .getId()
                                .equals(user.getId())
                        )
                )
                .map(user -> new UserGroupRelation(user, group))
                .toList());

        for (UserGroupRelation userGroupRelation : group.getGroupUsersRelations()) {
            entityManager.remove(userGroupRelation);
        }
        for (UserGroupRelation userGroupRelation : userGroupRelations) {
            entityManager.persist(userGroupRelation);
        }

        List<AccountGroupRelation> accountGroupRelations = new ArrayList<>(accounts.stream()
                .filter(account -> group.getAccountGroupsRelations()
                        .stream()
                        .noneMatch(accountGroupRelation -> accountGroupRelation
                                .getAccount()
                                .getId()
                                .equals(account.getId())
                        )
                )
                .map(account -> new AccountGroupRelation(account, group))
                .toList());

        for (AccountGroupRelation agr : group.getAccountGroupsRelations()) {
            entityManager.remove(agr);
        }
        for (AccountGroupRelation agr : accountGroupRelations) {
            entityManager.persist(agr);
        }

        group.setName(groupDto.getName());
        groupRepository.save(group);

        return group;
    }

    private String generateGroupCode(String name) {
        String now = String.valueOf(Instant.now().toEpochMilli());
        return name.substring(name.length() - 2).toUpperCase() + now.substring(now.length() - 3);
    }

    @Transactional
    public void removeUser(String groupId, String userId) {
        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        Group group = entityManager.find(Group.class, groupId);
        if (group == null) {
            throw new IllegalArgumentException(ErrorMessages.GROUP_DOESNT_EXIST.name());
        }

        Optional<UserGroupRelation> relation = group.getGroupUsersRelations()
                .stream()
                .filter(r -> r.getUser().equals(user))
                .findFirst();

        if (relation.isEmpty()) return;

        userGroupRelationRepository.delete(relation.get());
    }

    @Transactional
    public void addUser(String groupId, String userId) {
        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        Group group = entityManager.find(Group.class, groupId);
        if (group == null) {
            throw new IllegalArgumentException(ErrorMessages.GROUP_DOESNT_EXIST.name());
        }

        UserGroupRelation userGroupRelation = new UserGroupRelation(user, group);

        entityManager.persist(userGroupRelation);
    }

    @Transactional
    public void delete(String id, String userId) {
        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        Group group = entityManager.find(Group.class, id);
        if (group == null) {
            throw new IllegalArgumentException(ErrorMessages.GROUP_DOESNT_EXIST.name());
        }

        if (!group.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException(ErrorMessages.UNPERMITTED_OPERATION.name());
        }

        group.setRemoved(true);
        group.setRemovedAt(LocalDateTime.now());
        groupRepository.save(group);
    }
}
