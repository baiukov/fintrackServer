package me.vse.fintrackserver.services;

import jakarta.persistence.EntityManager;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.model.Group;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.model.UserGroupRelation;
import me.vse.fintrackserver.model.dto.GroupDto;
import me.vse.fintrackserver.repositories.AccountRepository;
import me.vse.fintrackserver.repositories.GroupRepository;
import me.vse.fintrackserver.repositories.UserGroupRelationRepository;
import me.vse.fintrackserver.repositories.UserRepository;
import org.apache.logging.log4j.util.Strings;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

    private EntityManager entityManager;
    private GroupService groupService;
    private UserRepository userRepository;
    private GroupRepository groupRepository;
    private UserGroupRelationRepository userGroupRelationRepository;
    private AccountRepository accountRepository;


    @BeforeEach
    public void setup() {
        entityManager = EasyMock.mock(EntityManager.class);
        userRepository = EasyMock.mock(UserRepository.class);
        groupRepository = EasyMock.mock(GroupRepository.class);
        userGroupRelationRepository = EasyMock.mock(UserGroupRelationRepository.class);
        accountRepository = EasyMock.mock(AccountRepository.class);
        groupService = new GroupService(entityManager, userRepository, groupRepository, accountRepository,
                userGroupRelationRepository);
    }

    private Stream<Arguments> getAddScenarios() {
        return Stream.of(
                Arguments.of(Group.builder()
                        .groupUsersRelations(List.of()).build(), ErrorMessages.INCORRECT_GROUP_NAME),
                Arguments.of(Group.builder()
                                .name("newGroup")
                                .groupUsersRelations(List.of())
                                .build(),
                        ErrorMessages.USER_DOESNT_EXIST),
                Arguments.of(Group.builder()
                                .name("newGroup")
                                .groupUsersRelations(List.of(
                                        new UserGroupRelation(User.builder().id("admin").build(), null)
                                ))
                                .build(),
                        null, ErrorMessages.USER_DOESNT_EXIST),
                Arguments.of(Group.builder()
                                .name("newGroup")
                                .groupUsersRelations(List.of(
                                        new UserGroupRelation(User.builder().id("admin").build(), null),
                                        new UserGroupRelation(User.builder().id("user1").build(), null),
                                        new UserGroupRelation(User.builder().id("user2").build(), null)
                                ))
                                .build(),
                        null, ErrorMessages.USER_DOESNT_EXIST)
        );
    }

    @ParameterizedTest(name = "Test group add. Given group: {0}. Should save or return exception: {1}")
    @MethodSource("getAddScenarios")
    public void addTest(Group group, ErrorMessages exception) {
        User admin = group.getGroupUsersRelations().isEmpty() ? null : group.getGroupUsersRelations().get(0).getUser();
        List<String> memberIds = group.getGroupUsersRelations()
                .stream()
                .map(UserGroupRelation::getUser)
                .map(User::getId)
                .toList();
        List<User> members = group.getGroupUsersRelations()
                .stream()
                .map(UserGroupRelation::getUser)
                .toList();

        GroupDto groupDto = new GroupDto(
                null,
                group.getName(),
                admin == null ? null : admin.getId(),
                memberIds,
                new ArrayList<>()
        );

        expect(entityManager.find(User.class, groupDto.getAdminId())).andReturn(admin);
        expect(userRepository.findUsers(groupDto.getMemberIds())).andReturn(members);
        entityManager.persist(anyObject(Group.class));
        expect(userGroupRelationRepository.saveAll(anyObject()))
                .andReturn(group.getGroupUsersRelations());
        replay(entityManager, userRepository, userGroupRelationRepository);

        if (exception != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> groupService.add(groupDto));
            assertEquals(exception.name(), thrown.getMessage());
        } else {
            Group actual = groupService.add(groupDto);
            assertEquals(group.getId(), actual.getId());
            assertEquals(group.getName(), actual.getName());
            assertEquals(group.getCreatedAt(), actual.getCreatedAt());
            assertEquals(group.getUpdatedAt(), actual.getUpdatedAt());
            verify(entityManager);
        }
    }

    private Stream<Arguments> getDeleteScenarios() {
        return Stream.of(
                Arguments.of(new Group(), ErrorMessages.GROUP_DOESNT_EXIST),
                Arguments.of(Group.builder().id("").build(), ErrorMessages.GROUP_DOESNT_EXIST),
                Arguments.of(Group.builder().id("groupId").build(), null),
                Arguments.of(Group.builder()
                                .id("groupId")
                                .name("group")
                                .code("GRP123")
                                .build(),
                        null)
        );
    }

    @ParameterizedTest(name = "Test group delete. Given old group {0}, expected group: {1}. " +
            "Should remove or throw exception {2}")
    @MethodSource("getDeleteScenarios")
    public void deleteTest(Group group, ErrorMessages message) {
        expect(entityManager.find(Group.class, group.getId()))
                .andReturn(StringUtils.isBlank(group.getId()) ? null : group);
        groupRepository.delete(group);
        replay(entityManager, groupRepository);

        if (message != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> groupService.delete(group.getId(), "userId"));
            assertEquals(message.name(), thrown.getMessage());
        } else {
            groupService.delete(group.getId(), "userId");
            verify(entityManager, groupRepository);
        }
    }

    private Stream<Arguments> removeUserScenarios() {
        return Stream.of(
                Arguments.of(new User(), new Group(), ErrorMessages.USER_DOESNT_EXIST),
                Arguments.of(User.builder().id("").build(), new Group(), ErrorMessages.USER_DOESNT_EXIST),
                Arguments.of(User.builder().id("user").build(),
                        Group.builder().id("").build(),
                        ErrorMessages.GROUP_DOESNT_EXIST),
                Arguments.of(User.builder().id("user").build(),
                        new Group(),
                        ErrorMessages.GROUP_DOESNT_EXIST),
                Arguments.of(User.builder()
                                .id("userId")
                                .build(),
                        Group.builder()
                                .id("groupId")
                                .groupUsersRelations(List.of(
                                                new UserGroupRelation(User.builder()
                                                        .id("userId")
                                                        .build(),
                                                        null)
                                        )
                                )
                                .build(),
                        null
                ),
                Arguments.of(User.builder()
                                .id("userId")
                                .build(),
                        Group.builder()
                                .id("groupId")
                                .groupUsersRelations(List.of())
                                .build(),
                        null
                ),
                Arguments.of(User.builder()
                                .id("otherUser")
                                .build(),
                        Group.builder()
                                .id("groupId")
                                .groupUsersRelations(List.of(
                                                new UserGroupRelation(User.builder()
                                                        .id("userId")
                                                        .build(),
                                                        null)
                                        )
                                )
                                .build(),
                        null
                ),
                Arguments.of(User.builder()
                                .id("userId")
                                .build(),
                        Group.builder()
                                .id("groupId")
                                .groupUsersRelations(List.of(
                                                new UserGroupRelation(User.builder()
                                                        .id("userId")
                                                        .build(),
                                                        null),
                                                new UserGroupRelation(User.builder()
                                                        .id("anotherUser")
                                                        .build(),
                                                        null)
                                        )
                                )
                                .build(),
                        null
                )
        );
    }

    @ParameterizedTest(name = "Test remove user from group. Given user: {0}, group: {1}. " +
            "Should remove user or throw exception: {2}")
    @MethodSource("removeUserScenarios")
    public void removeUserTest(User user, Group group, ErrorMessages message) {
        expect(entityManager.find(User.class, user.getId()))
                .andReturn(Strings.isBlank(user.getId()) ? null : user);
        expect(entityManager.find(Group.class, group.getId()))
                .andReturn(StringUtils.isBlank(group.getId()) ? null : group);
        userGroupRelationRepository.delete(anyObject());
        replay(entityManager, userGroupRelationRepository);

        if (message != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> groupService.removeUser(group.getId(), user.getId()));
            assertEquals(message.name(), thrown.getMessage());
        } else {
            groupService.removeUser(group.getId(), user.getId());
            verify(entityManager);
        }
    }

    private Stream<Arguments> addUserScenarios() {
        return Stream.of(
                Arguments.of(new User(), new Group(), ErrorMessages.USER_DOESNT_EXIST),
                Arguments.of(User.builder().id("").build(), new Group(), ErrorMessages.USER_DOESNT_EXIST),
                Arguments.of(User.builder().id("user").build(),
                        Group.builder().id("").build(),
                        ErrorMessages.GROUP_DOESNT_EXIST),
                Arguments.of(User.builder().id("user").build(),
                        new Group(),
                        ErrorMessages.GROUP_DOESNT_EXIST),
                Arguments.of(User.builder()
                                .id("userId")
                                .build(),
                        Group.builder()
                                .id("groupId")
                                .groupUsersRelations(List.of(
                                                new UserGroupRelation(User.builder()
                                                        .id("userId")
                                                        .build(),
                                                        null)
                                        )
                                )
                                .build(),
                        null
                ),
                Arguments.of(User.builder()
                                .id("userId")
                                .build(),
                        Group.builder()
                                .id("groupId")
                                .groupUsersRelations(List.of())
                                .build(),
                        null
                ),
                Arguments.of(User.builder()
                                .id("otherUser")
                                .build(),
                        Group.builder()
                                .id("groupId")
                                .groupUsersRelations(List.of(
                                                new UserGroupRelation(User.builder()
                                                        .id("userId")
                                                        .build(),
                                                        null)
                                        )
                                )
                                .build(),
                        null
                )
        );
    }

    @ParameterizedTest(name = "Test add user from group. Given user: {0}, group: {1}. " +
            "Should add user or throw exception: {2}")
    @MethodSource("removeUserScenarios")
    public void addUserTest(User user, Group group, ErrorMessages message) {
        expect(entityManager.find(User.class, user.getId()))
                .andReturn(Strings.isBlank(user.getId()) ? null : user);
        expect(entityManager.find(Group.class, group.getId()))
                .andReturn(StringUtils.isBlank(group.getId()) ? null : group);
        entityManager.persist(new UserGroupRelation(user, group));
        replay(entityManager);

        if (message != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> groupService.addUser(group.getId(), user.getId()));
            assertEquals(message.name(), thrown.getMessage());
        } else {
            groupService.addUser(group.getId(), user.getId());
            verify(entityManager);
        }
    }

    private Stream<Arguments> getAllAssetsScenarios() {
        return Stream.of(
                Arguments.of(new User(), List.of(), ErrorMessages.ACCOUNT_DOESNT_EXIST),
                Arguments.of(User.builder().id("").build(), List.of(), ErrorMessages.USER_DOESNT_EXIST),
                Arguments.of(User.builder().id("accId").build(),
                            List.of(Group.builder().id("groupId").build()),
                            null),
                Arguments.of(User.builder().id("accId").build(),
                            List.of(),
                            null),
                Arguments.of(User.builder().id("accId").build(),
                        List.of(Group.builder().id("groupId1").build()),
                        null),
                Arguments.of(User.builder().id("accId").build(),
                        List.of(
                                Group.builder().id("groupId1").build(),
                                Group.builder().id("groupId2").build(),
                                Group.builder().id("groupId3").build()
                        ),
                        null)
        );
    }

    @ParameterizedTest(name = "Test get all groups. Given user: {0}, groups: {1}. " +
            "Should return all groups or throw exception message {2}")
    @MethodSource("getAllAssetsScenarios")
    public void getAllTest(User user, List<Group> groups, ErrorMessages message) {
        List<UserGroupRelation> userGroupRelations = groups.stream()
                .map(group -> new UserGroupRelation(user, group))
                .toList();

        user.setUserGroupRelations(userGroupRelations);

        expect(entityManager.find(User.class, user.getId())).andReturn(Strings.isBlank(user.getId()) ? null : user);
        replay(entityManager);

        if (message != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> groupService.getAll(user.getId()));
            assertEquals(message.name(), thrown.getMessage());
        } else {
            assertEquals(groups, groupService.getAll(user.getId()));
        }
    }
}
