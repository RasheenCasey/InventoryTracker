package edu.vwcc.jdbc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.vwcc.jdbc.model.Purchase;

/*
 * Integration tests for the Purchase endpoints.
 * This test loads the full Spring Boot context (including the H2 embedded database seeded via data.sql)
 * and uses MockMvc to simulate HTTP requests.
 * The @ActiveProfiles("dev") annotation ensures it uses the H2 database configuration.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // each test method will execute in a rollback transaction for isolation
@ActiveProfiles("dev") // Use the 'dev' profile with H2 database
public class PurchaseIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	// ObjectMapper to serialize/deserialize JSON request/response content.
	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("GET /purchases returns initial purchases from data.sql")
	public void testGetPurchases() throws Exception {
		// Our data.sql seeds 5 purchases.
		mockMvc.perform(get("/purchases").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$", Matchers.hasSize(5)));
	}

	@Test
	@DisplayName("POST /purchases adds a new purchase and increases the count")
	public void testStorePurchase() throws Exception {
		// Create a new Purchase instance
		Purchase newPurchase = new Purchase();
		newPurchase.setProduct("Integration Test Product");
		newPurchase.setPrice(new BigDecimal("123.45"));

		String json = objectMapper.writeValueAsString(newPurchase);

		// POST the new purchase.
		mockMvc.perform(post("/purchases").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());

		// After inserting, a GET should return one additional record.
		mockMvc.perform(get("/purchases").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$", Matchers.hasSize(6)));
	}

	@Test
	@DisplayName("GET /purchases/count returns the correct number of purchases")
	public void testCountPurchases() throws Exception {
		mockMvc.perform(get("/purchases/count").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().string("5"));
	}

	@Test
	@DisplayName("GET /purchases/totalsales returns the sum of all purchase prices")
	public void testGetTotalSales() throws Exception {
		mockMvc.perform(get("/purchases/totalsales").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				// The sum of prices from data.sql is 262.95
				.andExpect(jsonPath("$", Matchers.comparesEqualTo(262.95)));
	}

	@Test
	@DisplayName("GET /purchases/avg returns the average purchase price")
	public void testGetAvgPurchasePrice() throws Exception {
		mockMvc.perform(get("/purchases/avg").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				// The average of prices from data.sql is 52.59
				.andExpect(jsonPath("$", Matchers.comparesEqualTo(52.59)));
	}

	@Test
	@DisplayName("GET /purchases/{id} returns the purchase with the specified id")
	public void testFindPurchaseById() throws Exception {
		mockMvc.perform(get("/purchases/1").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id", Matchers.is(1)))
				.andExpect(jsonPath("$.product", Matchers.is("Spring Boot in Action")))
				.andExpect(jsonPath("$.price", Matchers.comparesEqualTo(49.99)));
	}

	@Test
	@DisplayName("GET /purchases/{id} returns 404 when purchase doesn't exist")
	public void testFindPurchaseByIdNotFound() throws Exception {
		mockMvc.perform(get("/purchases/999").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("DELETE /purchases/{id} removes the purchase")
	public void testDeletePurchaseById() throws Exception {
		// Delete purchase with ID 1
		mockMvc.perform(delete("/purchases/1").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

		// Verify it's deleted by trying to fetch it
		mockMvc.perform(get("/purchases/1").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());

		// Verify count decreased
		mockMvc.perform(get("/purchases/count").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().string("4")); // One less than the initial 5
	}

	@Test
	@DisplayName("PATCH /purchases/{id}/nocost updates the purchase price to zero")
	public void testUpdatePurchaseToNoCost() throws Exception {
		// Update purchase with ID 1 to have no cost
		mockMvc.perform(patch("/purchases/1/nocost").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		// Verify price is now zero
		mockMvc.perform(get("/purchases/1").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.price", Matchers.comparesEqualTo(0.0)));

		// Verify total revenue decreased
		mockMvc.perform(get("/purchases/totalsales").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				// Original sum minus 49.99 = 212.96
				.andExpect(jsonPath("$", Matchers.comparesEqualTo(212.96)));
	}
}