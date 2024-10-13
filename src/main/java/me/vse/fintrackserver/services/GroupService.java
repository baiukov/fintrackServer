package me.vse.fintrackserver.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.model.Group;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.model.UserGroupRelation;
import me.vse.fintrackserver.model.dto.GroupDto;
import me.vse.fintrackserver.repositories.GroupRepository;
import me.vse.fintrackserver.repositories.UserGroupRelationRepository;
import me.vse.fintrackserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private UserGroupRelationRepository userGroupRelationRepository;

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

        return group;
    }

    private String generateGroupCode(String name) {
        String now = String.valueOf(Instant.now().toEpochMilli());
        return name.substring(name.length() - 2).toUpperCase() + now.substring(now.length() - 3);
    }
}
