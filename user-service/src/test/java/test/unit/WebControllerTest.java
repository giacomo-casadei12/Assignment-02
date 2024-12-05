package test.unit;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.http.ContentType;
import io.vertx.core.json.JsonObject;
import org.junit.BeforeClass;
import org.junit.Test;
import sap.ass02.userservice.domain.ports.AppManager;
import sap.ass02.userservice.infrastructure.WebController;
import sap.ass02.userservice.utils.WebOperation;
import test.unit.mocks.AppManagerMock;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.number.OrderingComparison.greaterThan;

public class WebControllerTest {

    private static final AppManager appManager = new AppManagerMock();

    private final JsonObject json = new JsonObject();

    @BeforeClass
    public static void setup() throws InterruptedException {
        RestAssured.baseURI = "http://127.0.0.1:8081/api";
        RestAssured.config = RestAssured.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", 5000) // Connection timeout in milliseconds
                        .setParam("http.socket.timeout", 5000));
        new WebController(appManager);

        Thread.sleep(10000);
    }

    @Test
    public void testLoginREST() {

        json.put("username", "giacomoc");
        json.put("password", "password");
        json.put("operation", WebOperation.LOGIN.ordinal());

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json.toString())
        .when()
                .get("/user/query")
        .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("result", equalTo("ok"));
    }

    @Test
    public void testUserGetAllREST() {

        json.put("operation", WebOperation.READ.ordinal());

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json.toString())
                .when()
                .get("/user/query")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("result.size()", greaterThan(0));
    }

    @Test
    public void testUserGetOneREST() {

        json.put("operation", WebOperation.READ.ordinal());
        json.put("userId", 1);

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json.toString())
                .when()
                .get("/user/query")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("userId", equalTo("1"));
    }

    @Test
    public void testCreateREST() {

        json.put("username", "test");
        json.put("password", "test");
        json.put("operation", WebOperation.CREATE.ordinal());

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json.toString())
                .when()
                .post("/user/command")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("result", equalTo("ok"));
    }

    @Test
    public void testUpdateREST() {

        json.put("userId", 1);
        json.put("credit", 2);
        json.put("operation", WebOperation.UPDATE.ordinal());

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json.toString())
                .when()
                .post("/user/command")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("result", equalTo("ok"));
    }

    @Test
    public void testDeleteREST() {

        json.put("userId", 1);
        json.put("operation", WebOperation.DELETE.ordinal());

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(json.toString())
                .when()
                .post("/user/command")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("result", equalTo("ok"));
    }

}
