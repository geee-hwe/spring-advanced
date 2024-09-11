package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ManagerServiceDeleteTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;

    @Test
    void manager_삭제에_성공한다() {
        //given
        AuthUser authUser = new AuthUser(1L, "user1@example.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        Manager manager = new Manager(todo.getUser(), todo);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(managerRepository.findById(anyLong())).willReturn(Optional.of(manager));

        // when & then
        assertDoesNotThrow(() -> managerService.deleteManager(authUser, 1L, 1L));
        verify(managerRepository, times(1)).delete(manager);
    }

    @Test
    void manager_삭제_중_todo를_찾지_못해_에러가_발생한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException thrown = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(authUser, 1L, 1L)
        );

        // then
        assertEquals("Todo not found", thrown.getMessage());
    }

    @Test
    void manager_삭제_중_user를_찾지_못해_에러가_발생한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        AuthUser anotherAuthUser = new AuthUser(2L, "b@b.com", UserRole.USER);
        User anotherUser = User.fromAuthUser(anotherAuthUser);
        Todo todo = new Todo("Title", "Contents", "Sunny", anotherUser);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));

        // when
        InvalidRequestException thrown = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(authUser, 1L, 1L)
        );

        // then
        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", thrown.getMessage());
    }

    @Test
    void manager_삭제_중_manager를_찾지_못해_에러가_발생한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(managerRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException thrown = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(authUser, 1L, 1L)
        );

        // then
        assertEquals("Manager not found", thrown.getMessage());
    }

    @Test
    void manager_삭제_중_manager와_todo의_manager가_일치하지_않아_에러가_발생한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        Todo differentTodo = new Todo();
        ReflectionTestUtils.setField(differentTodo, "id", 2L);
        Manager manager = new Manager(user, differentTodo);
        ReflectionTestUtils.setField(manager, "id", 1L);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(managerRepository.findById(anyLong())).willReturn(Optional.of(manager));

        // when
        InvalidRequestException thrown = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(authUser, differentTodo.getId(), manager.getId())
        );

        // then
        assertEquals("해당 일정에 등록된 담당자가 아닙니다.", thrown.getMessage());
    }

}
