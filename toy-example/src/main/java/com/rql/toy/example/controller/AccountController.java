package com.rql.toy.example.controller;

import com.rql.toy.example.controller.rest.PageRequestByExample;
import com.rql.toy.example.controller.rest.PageResponse;
import com.rql.toy.example.dto.AccountDto;
import com.rql.toy.example.dto.CommentDto;
import com.rql.toy.example.dto.PostDto;
import com.rql.toy.example.service.CommentService;
import com.rql.toy.example.service.DatabaseService;
import com.rql.toy.example.service.PostService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.rql.toy.example.controller.support.ControllerSupport;
import com.rql.toy.example.service.AccountService;


@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AccountController {

	private final AccountService accountService;

	private final CommentService commentService;

	private final PostService postService;

	private final DatabaseService databaseService;

	private final ControllerSupport controllerSupport;

	private Logger logger = LoggerFactory.getLogger(AccountController.class);

	@PostMapping(value = "/page")
	public ResponseEntity<PageResponse<AccountDto>> findAllAccounts(@RequestBody(required = false) PageRequestByExample<AccountDto> prbe,
																	@RequestParam(required = false) String... attributePaths) {

		prbe = prbe != null ? prbe : new PageRequestByExample<>();
		attributePaths = attributePaths == null ? new String[]{} : attributePaths;
		controllerSupport.defaultLazyLoadEvent(prbe);

		PageResponse<AccountDto> ret = accountService.findAllAccounts(prbe, attributePaths);
		return ResponseEntity.ok(ret);
	}

	@PostMapping(value = "/semi-standard")
	public ResponseEntity<PageResponse<AccountDto>> findAllAccountsJPA(@RequestBody(required = false) PageRequestByExample<AccountDto> prbe,
																	@RequestParam(required = false) String... attributePaths) {

		prbe = prbe != null ? prbe : new PageRequestByExample<>();
		attributePaths = attributePaths == null ? new String[]{} : attributePaths;
		controllerSupport.defaultLazyLoadEvent(prbe);

		PageResponse<AccountDto> ret = accountService.findAllAccountsJPA(prbe, attributePaths);
		return ResponseEntity.ok(ret);
	}

	@PostMapping(value = "/posts")
	public ResponseEntity<PageResponse<PostDto>> findAllPosts(@RequestBody(required = false) PageRequestByExample<PostDto> prbe,
															  @RequestParam(required = false) String... attributePaths) {

		prbe = prbe != null ? prbe : new PageRequestByExample<>();
		attributePaths = attributePaths == null ? new String[]{} : attributePaths;
		controllerSupport.defaultLazyLoadEvent(prbe);

		PageResponse<PostDto> ret = postService.findAllPosts(prbe, attributePaths);
		return ResponseEntity.ok(ret);
	}

	@PostMapping(value = "/comments")
	public ResponseEntity<PageResponse<CommentDto>> findAllComments(@RequestBody(required = false) PageRequestByExample<CommentDto> prbe,
																	@RequestParam(required = false) String... attributePaths) {

		prbe = prbe != null ? prbe : new PageRequestByExample<>();
		attributePaths = attributePaths == null ? new String[]{} : attributePaths;
		controllerSupport.defaultLazyLoadEvent(prbe);

		PageResponse<CommentDto> ret = commentService.findAllComments(prbe, attributePaths);
		return ResponseEntity.ok(ret);
	}


	@GetMapping("/{id}")
	public ResponseEntity<AccountDto> findOne(@PathVariable String id, @RequestParam(required = false) String... attributePaths) {
		attributePaths = attributePaths == null ? new String[]{} : attributePaths;
		AccountDto user = this.accountService.findOne(id, attributePaths);
		return ResponseEntity.ok(user);
	}

	@GetMapping("/db")
	public void populateDatabase() {
		databaseService.populateDatabase();
	}

	@GetMapping("/comments")
	public void populateComments() {
		databaseService.createComments();
	}

}
