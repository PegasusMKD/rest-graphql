package spring.graphql.rest.nonoptimized.core.nodes;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static spring.graphql.rest.nonoptimized.core.utility.NodeUtility.isPropertyRequested;

public class PropertyNodeFilters {

	public static boolean filterEagerAndRequiredManyToOne(List<String> propertyGraphPaths, String currentPath, boolean first, Field field) {
		ManyToOne relationalAnnotation = field.getAnnotation(ManyToOne.class);
		JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);

		if (relationalAnnotation == null) return false;

		boolean badlyConfiguredRelations = !relationalAnnotation.optional() || (joinColumnAnnotation != null && !joinColumnAnnotation.nullable());

		boolean requestedRelation = (first && isPropertyRequested(propertyGraphPaths, "", field));

		boolean subRelations = (
				!first && (
						relationalAnnotation.fetch() != FetchType.LAZY ||
								!Arrays.equals(relationalAnnotation.cascade(), new CascadeType[]{}) ||
								isPropertyRequested(propertyGraphPaths, currentPath + ".", field)
				)
		);

		return requestedRelation || badlyConfiguredRelations || subRelations;
	}

	public static boolean filterEagerAndRequiredOneToMany(List<String> propertyGraphPaths, String currentPath, boolean first, Field field) {
		OneToMany mainAnnotation = field.getAnnotation(OneToMany.class);
		if (mainAnnotation == null) return false;

		boolean requestedRelation = (first && isPropertyRequested(propertyGraphPaths, "", field));
		boolean subRelations = (!first && (mainAnnotation.fetch() != FetchType.LAZY ||
				isPropertyRequested(propertyGraphPaths, currentPath + ".", field)));

		return requestedRelation || subRelations;
	}

	public static boolean filterEagerAndRequiredOneToOne(List<String> propertyGraphPaths, String currentPath, boolean first, Field field) {
		OneToOne mainAnnotation = field.getAnnotation(OneToOne.class);
		if (mainAnnotation == null) return false;

		return !mainAnnotation.mappedBy().isEmpty() || isPropertyRequested(propertyGraphPaths, first ? "" : currentPath + ".", field);
	}


}