package spring.graphql.rest.nonoptimized.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import spring.graphql.rest.nonoptimized.core.rest.LazyLoadEvent;
import spring.graphql.rest.nonoptimized.core.rest.PageRequestByExample;
import spring.graphql.rest.nonoptimized.core.rest.PageResponse;
import spring.graphql.rest.nonoptimized.example.dto.AccountDto;
import spring.graphql.rest.nonoptimized.example.dto.CommentDto;
import spring.graphql.rest.nonoptimized.example.dto.PostDto;
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
	public ResponseEntity<PageResponse<AccountDto>> findAllAccounts(@RequestParam(required = false) String[] attributePaths,
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

		PageResponse<AccountDto> ret = accountService.findAllAccounts(prbe, attributePaths);
		return ResponseEntity.ok(ret);
	}

	@PostMapping(value = "/posts")
	public ResponseEntity<PageResponse<PostDto>> findAllPosts(@RequestParam(required = false) String[] attributePaths,
															@RequestBody(required = false) PageRequestByExample<PostDto> prbe) {
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
			prbe.setExample(new PostDto());
		}

		PageResponse<PostDto> ret = accountService.findAllPosts(prbe, attributePaths);
		return ResponseEntity.ok(ret);
	}

	@PostMapping(value = "/comments")
	public ResponseEntity<PageResponse<CommentDto>> findAllComments(@RequestParam(required = false) String[] attributePaths,
																	@RequestBody(required = false) PageRequestByExample<CommentDto> prbe) {
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
			prbe.setExample(new CommentDto());
		}

		PageResponse<CommentDto> ret = accountService.findAllComments(prbe, attributePaths);
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

	@GetMapping
	public void populateDatabase() {
		accountService.populateDatabase();
	}

}
