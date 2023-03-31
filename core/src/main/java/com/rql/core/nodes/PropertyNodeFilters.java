package com.rql.core.nodes;

import com.rql.core.utility.NodeUtility;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PropertyNodeFilters {

	private final NodeUtility nodeUtility;

	public boolean filterEagerAndRequiredManyToOne(List<String> propertyGraphPaths, String currentPath, boolean first, Field field) {
		ManyToOne relationalAnnotation = field.getAnnotation(ManyToOne.class);
		JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);

		if (relationalAnnotation == null) return false;

		boolean badlyConfiguredRelations = !relationalAnnotation.optional() || (joinColumnAnnotation != null && !joinColumnAnnotation.nullable());

		boolean requestedRelation = (first && nodeUtility.isPropertyRequested(propertyGraphPaths, "", field));

		boolean subRelations = (
				!first && (
						relationalAnnotation.fetch() != FetchType.LAZY ||
								!Arrays.equals(relationalAnnotation.cascade(), new CascadeType[]{}) ||
								nodeUtility.isPropertyRequested(propertyGraphPaths, currentPath + ".", field)
				)
		);

		return requestedRelation || badlyConfiguredRelations || subRelations;
	}

	public boolean filterEagerAndRequiredOneToMany(List<String> propertyGraphPaths, String currentPath, boolean first, Field field) {
		OneToMany mainAnnotation = field.getAnnotation(OneToMany.class);
		if (mainAnnotation == null) return false;

		boolean requestedRelation = (first && nodeUtility.isPropertyRequested(propertyGraphPaths, "", field));
		boolean subRelations = (!first && (mainAnnotation.fetch() != FetchType.LAZY ||
				nodeUtility.isPropertyRequested(propertyGraphPaths, currentPath + ".", field)));

		return requestedRelation || subRelations;
	}

	public boolean filterEagerAndRequiredOneToOne(List<String> propertyGraphPaths, String currentPath, boolean first, Field field) {
		OneToOne mainAnnotation = field.getAnnotation(OneToOne.class);
		if (mainAnnotation == null) return false;

		return !mainAnnotation.mappedBy().isEmpty() || nodeUtility.isPropertyRequested(propertyGraphPaths, first ? "" : currentPath + ".", field);
	}


}
