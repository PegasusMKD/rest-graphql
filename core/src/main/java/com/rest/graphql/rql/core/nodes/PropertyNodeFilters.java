package com.rest.graphql.rql.core.nodes;

import com.rest.graphql.rql.core.utility.NodeUtility;
import jakarta.persistence.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class PropertyNodeFilters {

	public static boolean filterEagerAndRequiredManyToOne(List<String> propertyGraphPaths, String currentPath, boolean first, Field field) {
		ManyToOne relationalAnnotation = field.getAnnotation(ManyToOne.class);
		JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);

		if (relationalAnnotation == null) return false;

		boolean badlyConfiguredRelations = !relationalAnnotation.optional() || (joinColumnAnnotation != null && !joinColumnAnnotation.nullable());

		boolean requestedRelation = (first && NodeUtility.isPropertyRequested(propertyGraphPaths, "", field));

		boolean subRelations = (
				!first && (
						relationalAnnotation.fetch() != FetchType.LAZY ||
								!Arrays.equals(relationalAnnotation.cascade(), new CascadeType[]{}) ||
								NodeUtility.isPropertyRequested(propertyGraphPaths, currentPath + ".", field)
				)
		);

		return requestedRelation || badlyConfiguredRelations || subRelations;
	}

	public static boolean filterEagerAndRequiredOneToMany(List<String> propertyGraphPaths, String currentPath, boolean first, Field field) {
		OneToMany mainAnnotation = field.getAnnotation(OneToMany.class);
		if (mainAnnotation == null) return false;

		boolean requestedRelation = (first && NodeUtility.isPropertyRequested(propertyGraphPaths, "", field));
		boolean subRelations = (!first && (mainAnnotation.fetch() != FetchType.LAZY ||
				NodeUtility.isPropertyRequested(propertyGraphPaths, currentPath + ".", field)));

		return requestedRelation || subRelations;
	}

	public static boolean filterEagerAndRequiredOneToOne(List<String> propertyGraphPaths, String currentPath, boolean first, Field field) {
		OneToOne mainAnnotation = field.getAnnotation(OneToOne.class);
		if (mainAnnotation == null) return false;

		return !mainAnnotation.mappedBy().isEmpty() || NodeUtility.isPropertyRequested(propertyGraphPaths, first ? "" : currentPath + ".", field);
	}


}
