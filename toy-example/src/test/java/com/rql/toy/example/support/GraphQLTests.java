package com.rql.toy.example.support;

// TODO: Refactor this so it's more readable (maybe using a hash map or something)
public class GraphQLTests {

	public static String cartesianAccounts() {
		return "{\n" +
				"    allAccounts(first:0, rows:20) {\n" +
				"        totalPages\n" +
				"        totalElements\n" +
				"        content {\n" +
				"            id\n" +
				"            username\n" +
				"            comments {\n" +
				"                id\n" +
				"                content\n" +
				"                post {\n" +
				"                    id\n" +
				"                    content\n" +
				"                    postedBy {\n" +
				"                        id\n" +
				"                        username\n" +
				"                    }\n" +
				"                }\n" +
				"                account {\n" +
				"                    id\n" +
				"                    username\n" +
				"                }\n" +
				"            }\n" +
				"            posts {\n" +
				"                id\n" +
				"                content\n" +
				"                postedBy {\n" +
				"                    id\n" +
				"                    username\n" +
				"                }\n" +
				"            }\n" +
				"        }\n" +
				"    }\n" +
				"}";
	}

	public static String basicAccounts() {
		return "{\n" +
				"    allAccounts {\n" +
				"        totalPages\n" +
				"        totalElements\n" +
				"        content {\n" +
				"            id,\n" +
				"            username\n" +
				"        }\n" +
				"    }\n" +
				"}";
	}

	public static String nestedAccounts() {
		return "{\n" +
				"    allAccounts {\n" +
				"        totalPages\n" +
				"        totalElements\n" +
				"        content {\n" +
				"            id\n" +
				"            username\n" +
				"            person {\n" +
				"                id\n" +
				"                fullName\n" +
				"                phoneNumber\n" +
				"                account {\n" +
				"                    id\n" +
				"                    username\n" +
				"                    person {\n" +
				"                        id\n" +
				"                        fullName\n" +
				"                        phoneNumber\n" +
				"                        account {\n" +
				"                            id\n" +
				"                            username\n" +
				"                        }\n" +
				"                    }\n" +
				"                }\n" +
				"            }\n" +
				"        }\n" +
				"    }\n" +
				"}";
	}
}
