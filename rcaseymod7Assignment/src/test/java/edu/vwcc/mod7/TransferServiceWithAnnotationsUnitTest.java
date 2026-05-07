package edu.vwcc.mod7;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransferServiceWithAnnotationsUnitTest {

	@Mock
	private AccountRepository accountRepository;

	@InjectMocks
	private TransferService transferService;

	@Test
	void exceptionWhenDestinationAccountNotFound() {
		Account sender = new Account();
		sender.setId(1);
		sender.setAmount(new BigDecimal("1000"));

		given(accountRepository.findById(1L)).willReturn(Optional.of(sender));
		given(accountRepository.findById(2L)).willReturn(Optional.empty());

		assertThrows(AccountNotFoundException.class, () -> transferService.transferMoney(1, 2, new BigDecimal("100")));

		verify(accountRepository, never()).changeAmount(anyLong(), any());
	}
}