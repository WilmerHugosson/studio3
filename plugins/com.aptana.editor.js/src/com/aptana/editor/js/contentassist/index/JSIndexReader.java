/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.js.contentassist.index;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.aptana.core.IMap;
import com.aptana.core.util.CollectionsUtil;
import com.aptana.core.util.RegexUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.js.JSTypeConstants;
import com.aptana.editor.js.contentassist.model.EventElement;
import com.aptana.editor.js.contentassist.model.FunctionElement;
import com.aptana.editor.js.contentassist.model.PropertyElement;
import com.aptana.editor.js.contentassist.model.TypeElement;
import com.aptana.index.core.Index;
import com.aptana.index.core.IndexReader;
import com.aptana.index.core.QueryResult;
import com.aptana.index.core.SearchPattern;

public class JSIndexReader extends IndexReader
{
	/**
	 * attachMembers
	 * 
	 * @param type
	 * @param index
	 * @throws IOException
	 */
	protected void attachMembers(TypeElement type, Index index)
	{
		// members
		if (type != null && index != null)
		{
			String typeName = type.getName();

			// properties
			for (PropertyElement property : this.getProperties(index, typeName))
			{
				type.addProperty(property);
			}

			// functions
			for (FunctionElement function : this.getFunctions(index, typeName))
			{
				type.addProperty(function);
			}

			// events
			for (EventElement event : this.getEvents(index, typeName))
			{
				type.addEvent(event);
			}
		}
	}

	/**
	 * createEvent
	 * 
	 * @param event
	 * @return
	 */
	protected EventElement createEvent(QueryResult event)
	{
		return this.populateElement(new EventElement(), event, 2);
	}

	/**
	 * createFunction
	 * 
	 * @param function
	 * @return
	 */
	protected FunctionElement createFunction(QueryResult function)
	{
		return this.populateElement(new FunctionElement(), function, 2);
	}

	/**
	 * createProperty
	 * 
	 * @param property
	 * @return
	 */
	protected PropertyElement createProperty(QueryResult property)
	{
		return this.populateElement(new PropertyElement(), property, 2);
	}

	/**
	 * createType
	 * 
	 * @param type
	 * @return
	 * @throws IOException
	 */
	protected TypeElement createType(QueryResult type)
	{
		TypeElement result;
		String[] columns = this.getDelimiterPattern().split(type.getWord());
		int column = 0;

		// create type
		result = new TypeElement();

		// name
		result.setName(columns[column]);
		column++;

		// super types
		if (column < columns.length)
		{
			for (String parentType : this.getSubDelimiterPattern().split(columns[column]))
			{
				result.addParentType(parentType);
			}
		}
		column++;

		// description
		if (column < columns.length)
		{
			result.setDescription(columns[column]);
		}
		column++;

		// documents
		for (String document : type.getDocuments())
		{
			result.addDocument(document);
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.index.core.IndexReader#getDelimiter()
	 */
	@Override
	protected String getDelimiter()
	{
		return IJSIndexConstants.DELIMITER;
	}

	/**
	 * getEvents
	 * 
	 * @param index
	 * @param owningTypes
	 * @return
	 * @throws IOException
	 */
	public List<EventElement> getEvents(Index index, List<String> owningTypes)
	{
		List<EventElement> result = new ArrayList<EventElement>();

		if (index != null && !CollectionsUtil.isEmpty(owningTypes))
		{
			// read events
			// @formatter:off
			List<QueryResult> events = index.query(
				new String[] { IJSIndexConstants.EVENT },
				this.getMemberPattern(owningTypes),
				SearchPattern.REGEX_MATCH
			);
			// @formatter:on

			result = CollectionsUtil.map(events, new IMap<QueryResult, EventElement>()
			{
				public EventElement map(QueryResult item)
				{
					return createEvent(item);
				}
			});
		}

		return result;
	}

	/**
	 * getEvents
	 * 
	 * @param index
	 * @param owningType
	 * @return
	 * @throws IOException
	 */
	public List<EventElement> getEvents(Index index, String owningType)
	{
		return getEvents(index, CollectionsUtil.newList(owningType));
	}

	/**
	 * getEvents
	 * 
	 * @param index
	 * @param owningType
	 * @param eventName
	 * @return
	 * @throws IOException
	 */
	public List<EventElement> getEvents(Index index, String owningType, String eventName)
	{
		List<EventElement> result = new ArrayList<EventElement>();

		if (index != null && !StringUtil.isEmpty(owningType) && !StringUtil.isEmpty(eventName))
		{
			// read events
			// @formatter:off
			List<QueryResult> events = index.query(
				new String[] { IJSIndexConstants.EVENT },
				this.getMemberPattern(owningType, eventName),
				SearchPattern.PREFIX_MATCH | SearchPattern.CASE_SENSITIVE
			);
			// @formatter:on

			result = CollectionsUtil.map(events, new IMap<QueryResult, EventElement>()
			{
				public EventElement map(QueryResult item)
				{
					return createEvent(item);
				}
			});
		}

		return result;
	}

	/**
	 * getFunctions
	 * 
	 * @param index
	 * @param owningTypes
	 * @return
	 * @throws IOException
	 */
	public List<FunctionElement> getFunctions(Index index, List<String> owningTypes)
	{
		List<FunctionElement> result = new ArrayList<FunctionElement>();

		if (index != null && !CollectionsUtil.isEmpty(owningTypes))
		{
			// read functions
			// @formatter:off
			List<QueryResult> functions = index.query(
				new String[] { IJSIndexConstants.FUNCTION },
				this.getMemberPattern(owningTypes),
				SearchPattern.REGEX_MATCH
			);
			// @formatter:on

			result = CollectionsUtil.map(functions, new IMap<QueryResult, FunctionElement>()
			{
				public FunctionElement map(QueryResult item)
				{
					return createFunction(item);
				}
			});
		}

		return result;
	}

	/**
	 * getFunctions
	 * 
	 * @param index
	 * @param owningType
	 * @return
	 * @throws IOException
	 */
	public List<FunctionElement> getFunctions(Index index, String owningType)
	{
		return getFunctions(index, CollectionsUtil.newList(owningType));
	}

	/**
	 * getFunction
	 * 
	 * @param index
	 * @param owningType
	 * @param propertyName
	 * @return
	 * @throws IOException
	 */
	public List<FunctionElement> getFunctions(Index index, String owningType, String propertyName)
	{
		List<FunctionElement> result = new ArrayList<FunctionElement>();

		if (index != null && !StringUtil.isEmpty(owningType) && !StringUtil.isEmpty(propertyName))
		{
			// @formatter:off
			List<QueryResult> functions = index.query(
				new String[] { IJSIndexConstants.FUNCTION },
				this.getMemberPattern(owningType, propertyName),
				SearchPattern.PREFIX_MATCH | SearchPattern.CASE_SENSITIVE
			);
			// @formatter:on

			result = CollectionsUtil.map(functions, new IMap<QueryResult, FunctionElement>()
			{
				public FunctionElement map(QueryResult item)
				{
					return createFunction(item);
				}
			});
		}

		return result;
	}

	/**
	 * getMemberPattern
	 * 
	 * @param typeNames
	 * @return
	 */
	private String getMemberPattern(List<String> typeNames)
	{
		typeNames = CollectionsUtil.map(typeNames, new IMap<String, String>()
		{
			public String map(String item)
			{
				return stripGenericsFromType(item);
			}
		});
		String typePattern = RegexUtil.createQuotedListPattern(typeNames);

		return MessageFormat.format("^{1}{0}", new Object[] { this.getDelimiter(), typePattern }); //$NON-NLS-1$
	}

	/**
	 * getMemberPattern
	 * 
	 * @param typeName
	 * @param memberName
	 * @return
	 */
	private String getMemberPattern(String typeName, String memberName)
	{
		return MessageFormat.format(
				"{1}{0}{2}{0}", new Object[] { this.getDelimiter(), stripGenericsFromType(typeName), memberName }); //$NON-NLS-1$
	}

	/**
	 * Looks for Array<?> and removes the type information for members.
	 * 
	 * @param typeName
	 * @return
	 */
	private String stripGenericsFromType(String typeName)
	{
		if (typeName.startsWith(JSTypeConstants.GENERIC_ARRAY_OPEN))
		{
			return JSTypeConstants.ARRAY_TYPE;
		}
		return typeName;
	}

	/**
	 * getProperties
	 * 
	 * @param index
	 * @param owningTypes
	 * @return
	 * @throws IOException
	 */
	public List<PropertyElement> getProperties(Index index, List<String> owningTypes)
	{
		List<PropertyElement> result = new ArrayList<PropertyElement>();

		if (index != null && !CollectionsUtil.isEmpty(owningTypes))
		{
			// read properties
			// @formatter:off
			List<QueryResult> properties = index.query(
				new String[] { IJSIndexConstants.PROPERTY },
				this.getMemberPattern(owningTypes),
				SearchPattern.REGEX_MATCH
			);
			// @formatter:on

			result = CollectionsUtil.map(properties, new IMap<QueryResult, PropertyElement>()
			{
				public PropertyElement map(QueryResult item)
				{
					return createProperty(item);
				}
			});
		}

		return result;
	}

	/**
	 * getProperties
	 * 
	 * @param index
	 * @param owningType
	 * @return
	 * @throws IOException
	 */
	public List<PropertyElement> getProperties(Index index, String owningType)
	{
		return getProperties(index, CollectionsUtil.newList(owningType));
	}

	/**
	 * getProperty
	 * 
	 * @param index
	 * @param owningType
	 * @param propertyName
	 * @return
	 * @throws IOException
	 */
	public List<PropertyElement> getProperties(Index index, String owningType, String propertyName)
	{
		List<PropertyElement> result = new ArrayList<PropertyElement>();

		if (index != null && !StringUtil.isEmpty(owningType) && !StringUtil.isEmpty(propertyName))
		{
			// @formatter:off
			List<QueryResult> properties = index.query(
				new String[] { IJSIndexConstants.PROPERTY },
				this.getMemberPattern(owningType, propertyName),
				SearchPattern.PREFIX_MATCH | SearchPattern.CASE_SENSITIVE
			);
			// @formatter:on

			result = CollectionsUtil.map(properties, new IMap<QueryResult, PropertyElement>()
			{
				public PropertyElement map(QueryResult item)
				{
					return createProperty(item);
				}
			});
		}

		return result;
	}

	/**
	 * getRequires
	 * 
	 * @param index
	 * @return
	 */
	public List<String> getRequires(Index index, final URI location)
	{
		final Set<String> result = new HashSet<String>();

		if (index != null)
		{
			// @formatter:off
			List<QueryResult> requires = index.query(
				new String[] { IJSIndexConstants.REQUIRE },
				"*", //$NON-NLS-1$
				SearchPattern.PATTERN_MATCH
			);
			// @formatter:on

			// build list of requires for specified location
			for (QueryResult item : getQueryResultsForLocation(requires, location))
			{
				for (String path : getSubDelimiterPattern().split(item.getWord()))
				{
					result.add(path);
				}
			}
		}

		return new ArrayList<String>(result);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.index.core.IndexReader#getSubDelimiter()
	 */
	@Override
	protected String getSubDelimiter()
	{
		return IJSIndexConstants.SUB_DELIMITER;
	}

	/**
	 * getType
	 * 
	 * @param index
	 * @param typeName
	 * @param includeMembers
	 * @return
	 */
	public List<TypeElement> getType(Index index, String typeName, boolean includeMembers)
	{
		List<TypeElement> result = new ArrayList<TypeElement>();

		if (index != null && !StringUtil.isEmpty(typeName))
		{
			String pattern = stripGenericsFromType(typeName) + this.getDelimiter();

			// @formatter:off
			List<QueryResult> types = index.query(
				new String[] { IJSIndexConstants.TYPE },
				pattern,
				SearchPattern.PREFIX_MATCH
			);
			// @formatter:on

			if (types != null)
			{
				for (QueryResult type : types)
				{
					TypeElement t = this.createType(type);

					if (includeMembers)
					{
						this.attachMembers(t, index);

						// make sure the newly created type can be serialized back to JSON in case it is modified
						t.setSerializeProperties(true);
					}

					result.add(t);
				}
			}
		}

		return result;
	}

	/**
	 * getTypeProperties
	 * 
	 * @param index
	 * @param typeName
	 * @return
	 * @throws IOException
	 */
	public List<PropertyElement> getTypeProperties(Index index, String typeName)
	{
		List<PropertyElement> properties = this.getProperties(index, typeName);

		properties.addAll(this.getFunctions(index, typeName));

		return properties;
	}

	/**
	 * getTypeNames
	 * 
	 * @param index
	 * @return
	 */
	public List<String> getTypeNames(Index index)
	{
		List<String> result = new ArrayList<String>();

		if (index != null)
		{
			// @formatter:off
			List<QueryResult> types = index.query(
				new String[] { IJSIndexConstants.TYPE },
				"*", //$NON-NLS-1$
				SearchPattern.PATTERN_MATCH
			);
			// @formatter:on

			if (types != null)
			{
				for (QueryResult type : types)
				{
					String word = type.getWord();
					int delimiterIndex = word.indexOf(getDelimiter());

					if (delimiterIndex != -1)
					{
						result.add(new String(word.substring(0, delimiterIndex)));
					}
					// else warn?
				}
			}
		}

		return result;
	}

	/**
	 * getTypes
	 * 
	 * @param index
	 * @param includeMembers
	 * @return
	 * @throws IOException
	 */
	public List<TypeElement> getTypes(Index index, boolean includeMembers)
	{
		List<TypeElement> result = Collections.emptyList();

		if (index != null)
		{
			// @formatter:off
			List<QueryResult> types = index.query(
				new String[] { IJSIndexConstants.TYPE },
				"*", //$NON-NLS-1$
				SearchPattern.PATTERN_MATCH
			);
			// @formatter:on

			if (types != null)
			{
				result = new ArrayList<TypeElement>();

				for (QueryResult type : types)
				{
					TypeElement t = this.createType(type);

					if (includeMembers)
					{
						this.attachMembers(t, index);

						t.setSerializeProperties(true);
					}

					result.add(t);
				}
			}
		}

		return result;
	}

	/**
	 * Convert a list of types into a regular expression. Note that this method assumes that list is non-empty
	 * 
	 * @param owningTypes
	 * @return
	 */
	protected String getUserTypesPattern(List<String> owningTypes)
	{
		List<String> quotedOwningTypes = new ArrayList<String>(owningTypes.size());

		// escape each owning type
		for (String owningType : owningTypes)
		{
			quotedOwningTypes.add(Pattern.quote(owningType));
		}

		// build pattern for all types
		return "(" + StringUtil.join("|", quotedOwningTypes) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
