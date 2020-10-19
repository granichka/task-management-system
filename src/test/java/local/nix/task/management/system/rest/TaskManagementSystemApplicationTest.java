package local.nix.task.management.system.rest;
import com.fasterxml.jackson.databind.JsonNode;
import local.nix.task.management.system.rest.config.security.SecurityConstants;
import local.nix.task.management.system.rest.model.task.request.SaveTaskRequest;
import local.nix.task.management.system.rest.model.task.response.TaskResponse;
import local.nix.task.management.system.rest.model.user.KnownAuthority;
import local.nix.task.management.system.rest.model.user.UserStatus;
import local.nix.task.management.system.rest.model.user.request.SaveUserRequest;
import local.nix.task.management.system.rest.model.user.response.UserResponse;
import local.nix.task.management.system.rest.model.user.security.request.UserLoginRequest;
import local.nix.task.management.system.rest.model.user.security.response.AccessTokenResponse;
import local.nix.task.management.system.rest.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.Arrays;


import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
public class TaskManagementSystemApplicationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testContextLoads() {
        assertNotEquals(0, port);
        assertNotNull(restTemplate);
    }

    @Test
    void createUserTest() {
        String username = "test_user123";
        String password = "12345678";
        String name = "Test User";
        ResponseEntity<UserResponse> userResponseResponseEntity = createUser(username, password, name);

        assertEquals(HttpStatus.CREATED, userResponseResponseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, userResponseResponseEntity.getHeaders().getContentType());

        UserResponse responseBody = userResponseResponseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(username, responseBody.getUsername());
        assertEquals(name, responseBody.getName());
        assertEquals(UserStatus.ACTIVE, responseBody.getUserStatus());
        assertTrue(responseBody.getAuthorities().contains(KnownAuthority.ROLE_USER));
        assertNotNull(responseBody.getId());

    }

    @Test
    void createInvalidUserTest() {
        ResponseEntity<?> blankUsernameResponse = createUser("", "12345678", "Test User");
        assertEquals(HttpStatus.BAD_REQUEST, blankUsernameResponse.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, blankUsernameResponse.getHeaders().getContentType());

        ResponseEntity<?> blankPasswordResponse = createUser("test_user", "", "Test User");
        assertEquals(HttpStatus.BAD_REQUEST, blankPasswordResponse.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, blankPasswordResponse.getHeaders().getContentType());

        ResponseEntity<?> invalidPasswordSizeResponse = createUser("test_user", "1234", "Test User");
        assertEquals(HttpStatus.BAD_REQUEST, invalidPasswordSizeResponse.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, invalidPasswordSizeResponse.getHeaders().getContentType());

        ResponseEntity<?> blankNameResponse = createUser("test_user", "12345678", "");
        assertEquals(HttpStatus.BAD_REQUEST, blankNameResponse.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, blankNameResponse.getHeaders().getContentType());
    }

    @Test
    void getTokenTest() {
        String username = "test_user";
        String password = "12345678";
        String name = "Test User";
        createUser(username, password, name);

        ResponseEntity<AccessTokenResponse> responseEntity = login(username, password);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());

        AccessTokenResponse response = responseEntity.getBody();
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        System.out.println(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getExpireIn());

    }

    @Test
    void getAllUsersWithTokenTest() throws IOException {
        String username = "test_user";
        String password = "12345678";
        String name = "Test User";
        createUser(username, password, name);


        ResponseEntity<AccessTokenResponse> responseEntity = login(username, password);
        AccessTokenResponse response = responseEntity.getBody();
        String accessToken = response.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", SecurityConstants.AUTH_TOKEN_PREFIX + accessToken);
        HttpEntity<String> entity = new HttpEntity<String>(headers);


        ResponseEntity<JsonNode> responseEntity1 =
                restTemplate.exchange(usersUrl(), HttpMethod.GET, entity, JsonNode.class);
        JsonNode responseNode = responseEntity1.getBody();
        JsonNode contentNode = responseNode.get("content");

        boolean hasCreatedUser = false;
        for (int i = 0; i < contentNode.size(); i++) {
            JsonNode userResponseNode = contentNode.get(i);
            String usernameOfCurrentNode = userResponseNode.get("username").asText();
            if(username.equals(usernameOfCurrentNode)) {
                hasCreatedUser = true;
            }
        }

        assertEquals(HttpStatus.OK, responseEntity1.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity1.getHeaders().getContentType());
        assertTrue(hasCreatedUser);
    }

    @Test
    void getAllUsersWithoutTokenTest() {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        ResponseEntity<JsonNode> responseEntity1 =
                restTemplate.exchange(usersUrl(), HttpMethod.GET, entity, JsonNode.class);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity1.getStatusCode());

    }

    @Test
    void createTaskWithAdminRoleTest() {
        String username = "test_user";
        String password = "12345678";
        String name = "Test User";
        createUser(username, password, name);

        String adminUsername = "granichka";
        String adminPassword = "extravaganza";
        String adminName = "Граница Юлия Андреевна";


        ResponseEntity<AccessTokenResponse> responseEntity = login(adminUsername, adminPassword);
        AccessTokenResponse response = responseEntity.getBody();

        String accessToken = response.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", SecurityConstants.AUTH_TOKEN_PREFIX + accessToken);


        String text = "исправить баг";
        String deadline = "2020-10-17 18:00:00";
        String executor = name;
        String reviewer = adminName;

        ResponseEntity<TaskResponse> taskResponseResponseEntity = createTask(text, deadline, executor, reviewer, headers);

        TaskResponse responseBody = taskResponseResponseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(name, responseBody.getExecutor());
        assertEquals(adminName, responseBody.getReviewer());
        assertEquals(text, responseBody.getText());
        assertEquals(TaskService.parseStringToLocalDateTime(deadline), responseBody.getDeadline());
        assertNotNull(responseBody.getId());


    }

    @Test
    void takeTaskTest() {
        String adminUsername = "granichka";
        String adminPassword = "extravaganza";
        String adminName = "Граница Юлия Андреевна";


        ResponseEntity<AccessTokenResponse> responseEntity = login(adminUsername, adminPassword);
        AccessTokenResponse response = responseEntity.getBody();

        String accessToken = response.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", SecurityConstants.AUTH_TOKEN_PREFIX + accessToken);


        String text = "исправить баг";
        String deadline = "2020-10-17 18:00:00";
        String executor = null;
        String reviewer = adminName;

        ResponseEntity<TaskResponse> taskResponseResponseEntity = createTask(text, deadline, executor, reviewer, headers);

        String username = "test_user";
        String password = "12345678";
        String name = "Test User";
        createUser(username, password, name);

        ResponseEntity<AccessTokenResponse> responseEntity1 = login(username, password);
        AccessTokenResponse response1 = responseEntity1.getBody();

        String accessToken1 = response1.getAccessToken();

        HttpHeaders headers1 = new HttpHeaders();
        headers1.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers1.set("Authorization", SecurityConstants.AUTH_TOKEN_PREFIX + accessToken1);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers1);

        RestTemplate restTemplate1 =
                new RestTemplate(new HttpComponentsClientHttpRequestFactory());

        ResponseEntity<TaskResponse> taskResponseResponseEntity1 =
                restTemplate1.exchange(tasksUrl() + "/1/take", HttpMethod.PATCH, httpEntity, TaskResponse.class);

        TaskResponse taskResponse = taskResponseResponseEntity1.getBody();
        assertEquals(name, taskResponse.getExecutor());



    }

    @Test
    void findUserByIdTest() {

        String username = "test_user";
        String password = "12345678";
        String name = "Test User";
        createUser(username, password, name);


        ResponseEntity<AccessTokenResponse> responseEntity = login(username, password);
        AccessTokenResponse response = responseEntity.getBody();
        String accessToken = response.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", SecurityConstants.AUTH_TOKEN_PREFIX + accessToken);
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        ResponseEntity<UserResponse> responseEntity1 =
                restTemplate.exchange(usersUrl() + "/1", HttpMethod.GET, entity, UserResponse.class);

        assertEquals(HttpStatus.OK, responseEntity1.getStatusCode());


    }



    private ResponseEntity<UserResponse> createUser(String username, String password, String name) {
        String url = usersUrl();
        SaveUserRequest requestBody = new SaveUserRequest();
        requestBody.setUsername(username);
        requestBody.setPassword(password);
        requestBody.setName(name);

        return restTemplate.postForEntity(url, requestBody, UserResponse.class);
    }

    private ResponseEntity<TaskResponse> createTask(String text, String deadline, String executor, String reviewer, HttpHeaders headers) {
        String url = tasksUrl();
        SaveTaskRequest requestBody = new SaveTaskRequest();
        requestBody.setText(text);
        requestBody.setDeadline(deadline);
        requestBody.setExecutor(executor);
        requestBody.setReviewer(reviewer);
        HttpEntity<SaveTaskRequest> entity = new HttpEntity<SaveTaskRequest>(requestBody, headers);

        return restTemplate.postForEntity(url, entity, TaskResponse.class);
    }

    private ResponseEntity<AccessTokenResponse> login(String username, String password) {
        UserLoginRequest requestBody = new UserLoginRequest();
        requestBody.setUsername(username);
        requestBody.setPassword(password);
        String url = baseUrl() + "/token";


        return restTemplate.postForEntity(url, requestBody, AccessTokenResponse.class);
    }

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    private String usersUrl() {
        return baseUrl() + "/users";
    }

    private String tasksUrl() {
        return baseUrl() + "/tasks";
    }


}
