package servicetest;

import io.cucumber.java.After;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.TestInstance;
import sap.ass02.bikeservice.infrastructure.Main;
import sap.ass02.bikeservice.utils.JsonFieldsConstants;
import sap.ass02.bikeservice.utils.WebOperation;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//BEFORE EXECUTING TESTS, change the url of db in sap.ass02.bikeservice.infrastructure.DataAccessL.EBikeDB
// from host.docker.internal to localhost

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StepDefinitions {

    private Response response;
    final private List<Integer> tempBikes = new ArrayList<>();
    final private List<Integer> foundBikes = new ArrayList<>();

    @BeforeAll
    public static void startServer(){
        Main.main(new String[]{});
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void deleteTempBikes(){
        tempBikes.forEach(x -> {
            JsonObject jsonBike = new JsonObject();
            jsonBike.put(JsonFieldsConstants.OPERATION, WebOperation.DELETE.ordinal());
            jsonBike.put(JsonFieldsConstants.E_BIKE_ID, x);
            response = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(jsonBike.toString())
                    .when()
                    .post("http://localhost:8082/api/ebike/command");
        });
    }

    @Given("the bike microservice is up and running")
    public void the_microservice_is_up_and_running() {
        // Optionally, you could check if the service is up by sending a health check request
        response = RestAssured.get("http://localhost:8082/healthCheck");
        assertEquals(200, response.getStatusCode());
    }

    @When("I send a GET request to {string} specifying the parameters for getting all bikes")
    public void iSendAGETRequestToSpecifyingTheParametersForGettingAllBikes(String path) {
        JsonObject json = new JsonObject();
        json.put(JsonFieldsConstants.OPERATION, WebOperation.READ.ordinal());
        response = RestAssured.given()
                        .contentType(ContentType.JSON)
                                .body(json.toString())
                                        .when()
                                            .get("http://localhost:8082" + path);
    }

    @Then("the response body should contain a list containing all bikes registered in the app")
    public void theResponseBodyShouldContainAListContainingAllBikesRegisteredInTheApp() {
        assertNotNull(response.jsonPath().getList("result"));
    }

    @And("there is a bike near the {int} {int} position")
    public void theIsABikeNearThePosition(int x, int y) {
        JsonObject json = new JsonObject();
        json.put(JsonFieldsConstants.OPERATION, WebOperation.READ.ordinal());
        json.put(JsonFieldsConstants.POSITION_X, x);
        json.put(JsonFieldsConstants.POSITION_Y, y);
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(json.toString())
                .when()
                .get("http://localhost:8082/api/ebike/query");
        if (response.jsonPath().getList("result").isEmpty()) {
            createANewBike(x, y);
            response = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(json.toString())
                    .when()
                    .get("http://localhost:8082/api/ebike/query");
            this.tempBikes.add(Integer.parseInt(response.jsonPath().getList("result").getFirst().toString().split("eBikeId=")[1].substring(0,1)));
        }
    }

    @When("I send a GET request to {string} specifying the parameters for getting all bikes near the {int} {int} position")
    public void iSendAGETRequestToSpecifyingTheParametersForGettingAllBikesNearThePosition(String path, int x, int y) {
        JsonObject json = new JsonObject();
        json.put(JsonFieldsConstants.OPERATION, WebOperation.READ.ordinal());
        json.put(JsonFieldsConstants.POSITION_X, x);
        json.put(JsonFieldsConstants.POSITION_Y, y);
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(json.toString())
                .when()
                .get("http://localhost:8082" + path);
    }

    @Then("the response body should contain a list containing all bikes near that position")
    public void theResponseBodyShouldContainAListContainingAllBikesNearThatPosition() {
        assertFalse(response.jsonPath().getList("result").isEmpty());
    }

    @And("there is an OUT OF CHARGE bike")
    public void thereIsAnOUTOFCHARGEBike() {
        JsonObject json = new JsonObject();
        json.put(JsonFieldsConstants.OPERATION, WebOperation.READ.ordinal());
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(json.toString())
                .when()
                .get("http://localhost:8082/api/ebike/query");
        List<Boolean> found = new ArrayList<>();
        response.jsonPath().getList("result").forEach(x -> {
            if (x.toString().contains("OUT_OF_CHARGE") ) {
                found.add(true);
                this.foundBikes.add(Integer.parseInt(x.toString().split("eBikeId=")[1].substring(0,1)));
            }
        });
        if (found.isEmpty()) {
            this.createANewBike(999, 999);
            this.foundBikes.add(read999Bike());
        }
    }

    private int read999Bike() {
        JsonObject json2 = new JsonObject();
        json2.put(JsonFieldsConstants.OPERATION, WebOperation.READ.ordinal());
        json2.put(JsonFieldsConstants.POSITION_X, 999);
        json2.put(JsonFieldsConstants.POSITION_Y, 999);
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(json2.toString())
                .when()
                .get("http://localhost:8082/api/ebike/query");
        int tempInt;
        tempInt = Integer.parseInt(response.jsonPath().getList("result").getFirst().toString().split("eBikeId=")[1].substring(0,1));
        setBikeToOutOfCharge(tempInt);
        this.tempBikes.add(tempInt);
        return tempInt;
    }

    @When("I send a POST request to {string} specifying the parameters for recharging that bike")
    public void iSendAPOSTRequestToSpecifyingTheParametersForRechargingThatBike(String path) {
        JsonObject jsonBike = new JsonObject();
        jsonBike.put(JsonFieldsConstants.OPERATION, WebOperation.UPDATE.ordinal());
        jsonBike.put(JsonFieldsConstants.E_BIKE_ID, foundBikes.getLast());
        jsonBike.put(JsonFieldsConstants.BATTERY, 100);
        jsonBike.put(JsonFieldsConstants.POSITION_X, 999);
        jsonBike.put(JsonFieldsConstants.POSITION_Y, 999);
        jsonBike.put(JsonFieldsConstants.STATE, "AVAILABLE");
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(jsonBike.toString())
                .when()
                .post("http://localhost:8082" + path);
    }

    @Then("the response body should contain whether the operation was successfully or not")
    public void theResponseBodyShouldContainWhetherTheOperationWasSuccessfullyOrNot() {
        assertEquals("ok",response.jsonPath().getString("result"));
    }

    @When("I send a POST request to {string} specifying the parameters for adding a new bike")
    public void iSendAPOSTRequestToSpecifyingTheParametersForAddingANewBike(String path) {
        JsonObject jsonBike = new JsonObject();
        jsonBike.put(JsonFieldsConstants.OPERATION, WebOperation.CREATE.ordinal());
        jsonBike.put(JsonFieldsConstants.POSITION_X, 999);
        jsonBike.put(JsonFieldsConstants.POSITION_Y, 999);
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(jsonBike.toString())
                .when()
                .post("http://localhost:8082" + path);
        Response tempRresponse;
        tempRresponse = response;
        this.tempBikes.add(read999Bike());
        response = tempRresponse;
    }

    private void createANewBike(int x, int y) {
        JsonObject jsonBike = new JsonObject();
        jsonBike.put(JsonFieldsConstants.OPERATION, WebOperation.CREATE.ordinal());
        jsonBike.put(JsonFieldsConstants.POSITION_X, x);
        jsonBike.put(JsonFieldsConstants.POSITION_Y, y);
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(jsonBike.toString())
                .when()
                .post("http://localhost:8082/api/ebike/command");
        assertEquals("ok",response.jsonPath().getString("result"));
    }

    private void setBikeToOutOfCharge(int tempInt) {
        JsonObject jsonBike = new JsonObject();
        jsonBike.put(JsonFieldsConstants.OPERATION, WebOperation.UPDATE.ordinal());
        jsonBike.put(JsonFieldsConstants.E_BIKE_ID, tempInt);
        jsonBike.put(JsonFieldsConstants.BATTERY, 0);
        jsonBike.put(JsonFieldsConstants.POSITION_X, 999);
        jsonBike.put(JsonFieldsConstants.POSITION_Y, 999);
        jsonBike.put(JsonFieldsConstants.STATE, "OUT_OF_CHARGE");
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(jsonBike.toString())
                .when()
                .post("http://localhost:8082/api/ebike/command");
        assertEquals("ok",response.jsonPath().getString("result"));
    }
}
