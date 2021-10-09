package spring.graphql.rest.nonoptimized.example.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import spring.graphql.rest.nonoptimized.example.controller.rest.PageRequestByExample;
import spring.graphql.rest.nonoptimized.example.controller.rest.PageResponse;
import spring.graphql.rest.nonoptimized.example.controller.support.ControllerSupport;
import spring.graphql.rest.nonoptimized.example.dto.AccountDto;
import spring.graphql.rest.nonoptimized.example.dto.CommentDto;
import spring.graphql.rest.nonoptimized.example.dto.PostDto;
import spring.graphql.rest.nonoptimized.example.service.AccountService;
import spring.graphql.rest.nonoptimized.example.service.CommentService;
import spring.graphql.rest.nonoptimized.example.service.DatabaseService;
import spring.graphql.rest.nonoptimized.example.service.PostService;


@CrossOrigin(origins = "*", maxAge = 3600)
@Controller
@RequestMapping("/users")
public class AccountController {

	private final AccountService accountService;

	private final CommentService commentService;

	private final PostService postService;

	private final DatabaseService databaseService;

	private Logger logger = LoggerFactory.getLogger(AccountController.class);

	public AccountController(AccountService accountService, CommentService commentService, PostService postService, DatabaseService databaseService) {
		this.accountService = accountService;
		this.commentService = commentService;
		this.postService = postService;
		this.databaseService = databaseService;
	}

	@PostMapping(value = "/page")
	public ResponseEntity<PageResponse<AccountDto>> findAllAccounts(@RequestBody(required = false) PageRequestByExample<AccountDto> prbe,
																	@RequestParam(required = false) String... attributePaths) {

		prbe = prbe != null ? prbe : new PageRequestByExample<>();
		attributePaths = attributePaths == null ? new String[]{} : attributePaths;
		ControllerSupport.defaultLazyLoadEvent(prbe);

//		long startTime = System.nanoTime();
		PageResponse<AccountDto> ret = accountService.findAllAccounts(prbe, attributePaths);
//		long endTime = System.nanoTime();
//		logger.info("Total time: {} s", (endTime - startTime) / 1000000000.);
		return ResponseEntity.ok(ret);
	}

	@PostMapping(value = "/posts")
	public ResponseEntity<PageResponse<PostDto>> findAllPosts(@RequestBody(required = false) PageRequestByExample<PostDto> prbe,
															  @RequestParam(required = false) String... attributePaths) {

		prbe = prbe != null ? prbe : new PageRequestByExample<>();
		attributePaths = attributePaths == null ? new String[]{} : attributePaths;
		ControllerSupport.defaultLazyLoadEvent(prbe);

		PageResponse<PostDto> ret = postService.findAllPosts(prbe, attributePaths);
		return ResponseEntity.ok(ret);
	}

	@PostMapping(value = "/comments")
	public ResponseEntity<PageResponse<CommentDto>> findAllComments(@RequestBody(required = false) PageRequestByExample<CommentDto> prbe,
																	@RequestParam(required = false) String... attributePaths) {

		prbe = prbe != null ? prbe : new PageRequestByExample<>();
		attributePaths = attributePaths == null ? new String[]{} : attributePaths;
		ControllerSupport.defaultLazyLoadEvent(prbe);

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
