package org.example.expert.domain.todo.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @InjectMocks
    private TodoService todoService;
    @Mock
    private WeatherClient weatherClient;
    @Mock
    private TodoRepository todoRepository;


    @Test
    public void todo를_정상적으로_등록한다() {
        // given
        String weather = "Sunny";
        given(weatherClient.getTodayWeather()).willReturn(weather);

        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("Title", "Contents");
        Todo savedTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        long savedTodoId = 1L;
        ReflectionTestUtils.setField(savedTodo, "id", savedTodoId);
        given(todoRepository.save(any())).willReturn(savedTodo);

        // when
        TodoSaveResponse response = todoService.saveTodo(authUser, todoSaveRequest);

        // then
        assertNotNull(response);
        assertEquals(savedTodo.getId(), response.getId());
    }
}
