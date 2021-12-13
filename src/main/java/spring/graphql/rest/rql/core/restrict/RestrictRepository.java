package spring.graphql.rest.rql.core.restrict;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import spring.graphql.rest.rql.core.nodes.PropertyNode;
import spring.graphql.rest.rql.example.models.Account;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static spring.graphql.rest.rql.core.utility.GraphUtility.createPropertyNodes;

@Component
public class RestrictRepository {

	private final HashMap<String, Set<String>> bannedKeywords = new HashMap<>();
	private final HashSet<String> bannedChildKeywords = new HashSet<>();

	@Value("${rql.base-model-package}")
	public String baseModelPackage;

	@PostConstruct
	public void init() {
		ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
		componentProvider.addIncludeFilter(new AnnotationTypeFilter(RQLRestrict.class));
		for (BeanDefinition bd : componentProvider.findCandidateComponents(baseModelPackage)) {
			AnnotationMetadata metadata = ((AnnotatedBeanDefinition) bd).getMetadata();

			// TODO: Fix this so that it doesn't require this specific naming schema
			String[] splitName = ((AnnotatedBeanDefinition) bd).getMetadata().getClassName().split("\\.");
			String forcedRQLName = splitName[splitName.length - 1].toLowerCase(Locale.ROOT) + "s";
			bannedKeywords.put(forcedRQLName,
					new HashSet<>(Arrays.asList(metadata.getAnnotations().get(RQLRestrict.class).getStringArray("value"))));
		}

		bannedKeywords.forEach((key, set) -> bannedChildKeywords.addAll(set.stream().map(value -> key + "." + value).collect(Collectors.toList())));
	}

	public void filter(Class<?> returnType, String... attributePaths) {
		filter(returnType.getSimpleName().toLowerCase(Locale.ROOT) + "s", attributePaths);
	}

	public void filter(String parent, String... attributePaths) {
		List<String> attr = Arrays.asList(attributePaths);
		List<String> compiled = createPropertyNodes(Account.class, attributePaths)
				.stream().map(PropertyNode::getGraphPath).collect(Collectors.toList());

		// TODO: Add better exceptions
		if (attr.stream().anyMatch(prop -> isBannedKeyword(prop, parent))) {
			throw new RuntimeException("400: Request error");
		} else if (compiled.stream().anyMatch(prop -> isBannedKeyword(prop, parent))) {
			throw new RuntimeException("500: Entity Configuration error");
		}
	}

	private boolean isBannedKeyword(String item, String parent) {
		return bannedChildKeywords.stream().anyMatch(item::endsWith) || bannedKeywords.get(parent).contains(item);
	}

}
