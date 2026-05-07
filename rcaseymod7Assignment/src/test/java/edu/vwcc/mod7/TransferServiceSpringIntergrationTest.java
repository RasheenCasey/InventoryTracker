package edu.vwcc.mod7;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class TransferServiceSpringIntergrationTest {
	
	@MockitoBean
	AccountRepository accountRepository = mock(AccountRepository.class);
	
	@Autowired
	TransferService transferService = new TransferService(accountRepository);
	
	
	
	
	@Test
	void transferServiceSpringIntergrationTest() {
		
	
	Account sender = new Account();
	sender.setId(1);
	sender.setAmount(new BigDecimal(1000));
	
	Account destination = new Account();
	destination.setId(2);
	destination.setAmount (new BigDecimal(1000));
	
	when(accountRepository.findById(1L)).thenReturn(Optional.of(sender));
	when(accountRepository.findById(2L)).thenReturn(Optional.of(destination));
	
	transferService.transferMoney(1, 2, new BigDecimal(100));
	
	verify(accountRepository).changeAmount(1, new BigDecimal(900));
	verify(accountRepository).changeAmount(2, new BigDecimal(1100));
	
}
}