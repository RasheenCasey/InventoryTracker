package edu.vwcc.jdbc.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import edu.vwcc.jdbc.model.Purchase;
import edu.vwcc.jdbc.repo.PurchaseRepository;

/*
 * Unit tests for the PurchaseController.
 * The PurchaseRepository dependency is mocked using Mockito.
 */
@ExtendWith(MockitoExtension.class)
public class PurchaseControllerUnitTests {

	@Mock
	private PurchaseRepository purchaseRepository;

	@InjectMocks
	private PurchaseController purchaseController;

	@Test
	@DisplayName("storePurchase: should call the repository's storePurchase method")
	public void testStorePurchase() {
		// Prepare a sample purchase.
		Purchase purchase = new Purchase();
		purchase.setId(1);
		purchase.setProduct("Test Product");
		purchase.setPrice(new BigDecimal("99.99"));

		// Call the controller endpoint.
		purchaseController.storePurchase(purchase);

		// Verify that the repository's storePurchase was invoked with the correct
		// purchase.
		verify(purchaseRepository).storePurchase(purchase);
	}

	@Test
	@DisplayName("findPurchase: should return a list of purchases as returned by the repository")
	public void testFindPurchases() {
		// Prepare two dummy purchase instances.
		Purchase p1 = new Purchase();
		p1.setId(1);
		p1.setProduct("Product 1");
		p1.setPrice(new BigDecimal("10.00"));

		Purchase p2 = new Purchase();
		p2.setId(2);
		p2.setProduct("Product 2");
		p2.setPrice(new BigDecimal("20.00"));

		List<Purchase> mockPurchases = Arrays.asList(p1, p2);
		// Stub the repository so that findAllPurchases returns our dummy data.
		when(purchaseRepository.findAllPurchases()).thenReturn(mockPurchases);

		// Execute the controller method.
		List<Purchase> result = purchaseController.findPurchase();

		// Validate results.
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("Product 1", result.get(0).getProduct());
		assertEquals("Product 2", result.get(1).getProduct());
	}

	@Test
	@DisplayName("countPurchases: should return the count from the repository")
	public void testCountPurchases() {
		// Stub the repository to return a specific count
		when(purchaseRepository.countAllPurchases()).thenReturn(5);

		// Call the controller method
		int count = purchaseController.countPurchases();

		// Verify the result
		assertEquals(5, count);
		verify(purchaseRepository).countAllPurchases();
	}

	@Test
	@DisplayName("getTotalSales: should return the total sales amount from the repository")
	public void testGetTotalSales() {
		// Stub the repository to return a specific total
		when(purchaseRepository.getTotalRevenue()).thenReturn(new BigDecimal("250.97"));

		// Call the controller method
		BigDecimal total = purchaseController.getTotalSales();

		// Verify the result
		assertEquals(new BigDecimal("250.97"), total);
		verify(purchaseRepository).getTotalRevenue();
	}

	@Test
	@DisplayName("getAvgPurchasePrice: should return the average price from the repository")
	public void testGetAvgPurchasePrice() {
		// Stub the repository to return a specific average
		when(purchaseRepository.getAvgPurchasePrice()).thenReturn(new BigDecimal("42.50"));

		// Call the controller method
		BigDecimal avg = purchaseController.getAvgPurchasePrice();

		// Verify the result
		assertEquals(new BigDecimal("42.50"), avg);
		verify(purchaseRepository).getAvgPurchasePrice();
	}

	@Test
	@DisplayName("findPurchaseById: should return a purchase when it exists")
	public void testFindPurchaseById() {
		// Prepare a purchase
		Purchase purchase = new Purchase();
		purchase.setId(1);
		purchase.setProduct("Test Product");
		purchase.setPrice(new BigDecimal("99.99"));

		// Stub the repository to return the purchase directly
		when(purchaseRepository.findPurchaseById(1)).thenReturn(purchase);

		// Call the controller method
		Purchase result = purchaseController.findPurchaseById(1);

		// Verify the result
		assertNotNull(result);
		assertEquals(1, result.getId());
		assertEquals("Test Product", result.getProduct());
		assertEquals(new BigDecimal("99.99"), result.getPrice());
		verify(purchaseRepository).findPurchaseById(1);
	}

	@Test
	@DisplayName("findPurchaseById: should throw ResponseStatusException when purchase doesn't exist")
	public void testFindPurchaseByIdNotFound() {
		// Stub the repository to return null
		when(purchaseRepository.findPurchaseById(999)).thenReturn(null);

		// Call the controller method and expect an exception
		Exception exception = assertThrows(ResponseStatusException.class, () -> {
			purchaseController.findPurchaseById(999);
		});

		// Verify the exception is a NOT_FOUND status
		assertTrue(exception instanceof ResponseStatusException);
		ResponseStatusException responseException = (ResponseStatusException) exception;
		assertEquals(HttpStatus.NOT_FOUND, responseException.getStatusCode());

		// Verify the repository method was called
		verify(purchaseRepository).findPurchaseById(999);
	}

	@Test
	@DisplayName("deletePurchaseById: should delete the purchase with the given id")
	public void testDeletePurchaseById() {
		// Stub the repository method
		doNothing().when(purchaseRepository).deletePurchaseById(1);

		// Call the controller method
		purchaseController.deletePurchaseById(1);

		// Verify repository was called
		verify(purchaseRepository).deletePurchaseById(1);
	}

	@Test
	@DisplayName("updatePurchaseToNoCost: should update the purchase price to zero")
	public void testUpdatePurchaseToNoCost() {
		// Stub the repository method
		doNothing().when(purchaseRepository).updatePurchaseToNoCost(1);

		// Call the controller method
		purchaseController.updatePurchaseToNoCost(1);

		// Verify repository was called
		verify(purchaseRepository).updatePurchaseToNoCost(1);
	}
}