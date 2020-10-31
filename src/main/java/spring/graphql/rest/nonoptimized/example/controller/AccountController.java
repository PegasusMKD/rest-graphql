package spring.graphql.rest.nonoptimized.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import spring.graphql.rest.nonoptimized.core.rest.LazyLoadEvent;
import spring.graphql.rest.nonoptimized.core.rest.PageRequestByExample;
import spring.graphql.rest.nonoptimized.core.rest.PageResponse;
import spring.graphql.rest.nonoptimized.example.dto.AccountDto;
import spring.graphql.rest.nonoptimized.example.service.AccountService;


@CrossOrigin(origins = "*", maxAge = 3600)
@Controller
@RequestMapping("/users")
public class AccountController {

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@PostMapping(value = "/page")
	public ResponseEntity<PageResponse<AccountDto>> findAll(@RequestParam(required = false) String[] attributePaths,
															@RequestBody(required = false) PageRequestByExample<AccountDto> prbe) {
		if(prbe == null) {
			prbe = new PageRequestByExample<>();
		}

		if(attributePaths == null) {
			attributePaths = new String[]{};
		}

		LazyLoadEvent lazyLoadEvent = new LazyLoadEvent();
		if (prbe.getLazyLoadEvent() == null) {
			lazyLoadEvent.setFirst(0);
			lazyLoadEvent.setRows(Integer.MAX_VALUE);
			prbe.setLazyLoadEvent(lazyLoadEvent);
		}

		if(prbe.getExample() == null) {
			prbe.setExample(new AccountDto());
		}

		PageResponse<AccountDto> ret = accountService.findAll(prbe, attributePaths);
		return ResponseEntity.ok(ret);
	}


	@GetMapping("/{id}")
	public ResponseEntity<AccountDto> findOne(@PathVariable String id, @RequestParam(required = false) String[] attributePaths) {
		if(attributePaths == null) {
			attributePaths = new String[]{};
		}
		AccountDto user = this.accountService.findOne(id, attributePaths);
		return ResponseEntity.ok(user);
	}

}
