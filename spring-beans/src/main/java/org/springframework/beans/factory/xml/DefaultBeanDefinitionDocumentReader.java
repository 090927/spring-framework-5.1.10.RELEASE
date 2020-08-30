/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the {@link BeanDefinitionDocumentReader} interface that
 * reads bean definitions according to the "spring-beans" DTD and XSD format
 * (Spring's default XML bean definition format).
 *
 * <p>The structure, elements, and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary
 * to produce this format). {@code <beans>} does not need to be the root
 * element of the XML document: this class will parse all bean definition elements
 * in the XML file, regardless of the actual root element.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Erik Wiersma
 * @since 18.12.2003
 *
 * Document 读取器。
 */
public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {

	public static final String BEAN_ELEMENT = BeanDefinitionParserDelegate.BEAN_ELEMENT;

	public static final String NESTED_BEANS_ELEMENT = "beans";

	public static final String ALIAS_ELEMENT = "alias";

	public static final String NAME_ATTRIBUTE = "name";

	public static final String ALIAS_ATTRIBUTE = "alias";

	public static final String IMPORT_ELEMENT = "import";

	public static final String RESOURCE_ATTRIBUTE = "resource";

	public static final String PROFILE_ATTRIBUTE = "profile";


	protected final Log logger = LogFactory.getLog(getClass());

	@Nullable
	private XmlReaderContext readerContext;

	@Nullable
	private BeanDefinitionParserDelegate delegate;


	/**
	 * This implementation parses bean definitions according to the "spring-beans" XSD
	 * (or DTD, historically).
	 * <p>Opens a DOM Document; then initializes the default settings
	 * specified at the {@code <beans/>} level; then parses the contained bean definitions.
	 *
	 * TODO IOC 【3、注册阶段】~ 将配置解析成 BeanDefinition 并注册。
	 */
	@Override
	public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
		// 将获得 XML 描述符
		this.readerContext = readerContext;

		/**
		 *  【核心逻辑】解析bean 文档对象 {@link #doRegisterBeanDefinitions(Element)}
		 */
		doRegisterBeanDefinitions(doc.getDocumentElement());
	}

	/**
	 * Return the descriptor for the XML resource that this parser works on.
	 */
	protected final XmlReaderContext getReaderContext() {
		Assert.state(this.readerContext != null, "No XmlReaderContext available");
		return this.readerContext;
	}

	/**
	 * Invoke the {@link org.springframework.beans.factory.parsing.SourceExtractor}
	 * to pull the source metadata from the supplied {@link Element}.
	 */
	@Nullable
	protected Object extractSource(Element ele) {
		return getReaderContext().extractSource(ele);
	}


	/**
	 * Register each bean definition within the given root {@code <beans/>} element.
	 */
	@SuppressWarnings("deprecation")  // for Environment.acceptsProfiles(String...)
	protected void doRegisterBeanDefinitions(Element root) {
		// Any nested <beans> elements will cause recursion in this method. In
		// order to propagate and preserve <beans> default-* attributes correctly,
		// keep track of the current (parent) delegate, which may be null. Create
		// the new (child) delegate with a reference to the parent for fallback purposes,
		// then ultimately reset this.delegate back to its original (parent) reference.
		// this behavior emulates a stack of delegates without actually necessitating one.
		/**
		 *
		 * 具体解析过程  `createDelegate`
		 *
		 * 1、BeanDefinitionParserDelegate 中定义 Spring bean 定义XML 文件的各种元素。
		 *   实现类 {@link BeanDefinitionParserDelegate#initDefaults(Element)}
		 *
		 */
		BeanDefinitionParserDelegate parent = this.delegate;
		this.delegate = createDelegate(getReaderContext(), root, parent);

		if (this.delegate.isDefaultNamespace(root)) {

			/*
			 * 检查是否定义了profile属性，如果定义了需要到环境变量中找，利用这个特性我们可以在配置文件中部署不同的环境
			 */
			String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
			if (StringUtils.hasText(profileSpec)) {
				String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
						profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
				// We cannot use Profiles.of(...) since profile expressions are not supported
				// in XML config. See SPR-12458 for details.
				if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Skipped XML bean definition file due to specified profiles [" + profileSpec +
								"] not matching: " + getReaderContext().getResource());
					}
					return;
				}
			}
		}
		/*
		 * 在解析 Bean 定义之前，进行自定义解析，增强解析过程可扩展性。由子类实现。
		 */
		preProcessXml(root);

		/**
		 *  使用 Spring 的Bean 规则从文档的根元素开始
		 *  【核心】bean 定义的文档对象的解析并注册 {@link #parseBeanDefinitions(Element, BeanDefinitionParserDelegate)}
		 */
		parseBeanDefinitions(root, this.delegate);

		/*
		 * 在解析 bean 定义之后，进行自定义解析，增加解析过程的可扩展性。由子类实现
		 */
		postProcessXml(root);

		this.delegate = parent;
	}

	/**
	 *
	 * 创建 BeanDefinitionParserDelegate， 完成真正的解析过程。
	 */
	protected BeanDefinitionParserDelegate createDelegate(
			XmlReaderContext readerContext, Element root, @Nullable BeanDefinitionParserDelegate parentDelegate) {

		BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegate(readerContext);

		/**
		 * {@link BeanDefinitionParserDelegate#initDefaults(Element)}
		 */
		delegate.initDefaults(root, parentDelegate);
		return delegate;
	}

	/**
	 * Parse the elements at the root level in the document:
	 * "import", "alias", "bean".
	 * @param root the DOM root element of the document
	 *
	 *  使用 Spring 的Bean 规则从文档的跟元素开始 bean 定义的文档对象的解析
	 */
	protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {

		/*
		 * bean 定义的文档对象使用,是默认命名空间 xmlns="http://www.springframework.org/schema/beans"
		 */
		if (delegate.isDefaultNamespace(root)) {
			// 获取 bean 定义文档的 跟元素的所有子节点。
			NodeList nl = root.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);

				// 获取文档中，XML 元素节点
				if (node instanceof Element) {
					Element ele = (Element) node;

					// 默认标签解析 如<bean class=""/>
					if (delegate.isDefaultNamespace(ele)) {

						/**
						 * 使用 Spring Bean 规则解析元素节点 {@link #parseDefaultElement(Element, BeanDefinitionParserDelegate)}
						 */
						parseDefaultElement(ele, delegate);
					}
					else {

						/**
						 *  <tx: annotation-driven/> 自定义的解析策略。
						 *
						 * 如果没有使用 Spring 默认的 XML 命名空间，则使用用户自定义的解析规则解析元素节点。{@link BeanDefinitionParserDelegate#parseCustomElement(Element, BeanDefinition)}
						 */
						delegate.parseCustomElement(ele);
					}
				}
			}
		}
		else {

			/*
			 * 文档的根节点，没有使用 Spring 默认命名空间（xmlns="http://www.springframework.org/schema/beans"）使用自定义 的解析规则解析文档的根节点。
			 */
			delegate.parseCustomElement(root);
		}
	}

	/**
	 * 使用 Spring Bean 规则解析元素节点, 【 默认标签解析 】
	 * TODO IOC 解析 <import>、<alias>、<bean> 元素
	 */
	private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {

		/**
		 * 解析<import> 导入元素 {@link #importBeanDefinitionResource
		 */
		if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
			importBeanDefinitionResource(ele);
		}

		/**
		 * 解析 <alias> 进行别名解析
		 *
		 * {@link #processAliasRegistration
		 */
		else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
			processAliasRegistration(ele);
		}

		/**
		 * 【最重要 ~ 注册IOC容器 】
		 *  解析 <bean> 元素，按照Spring 的Bean 规则进行解析 {@link #processBeanDefinition
		 */
		else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
			processBeanDefinition(ele, delegate);
		}

		/**
		 * 解析 <beans></beans>
		 */
		else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
			// recurse
			doRegisterBeanDefinitions(ele);
		}
	}

	/**
	 * Parse an "import" element and load the bean definitions
	 * from the given resource into the bean factory.
	 *
	 * TODO 解析 “import” 导入元素。从给定的导入路径加载 Bean 资源到 Spring IOC 容器中
	 */
	protected void importBeanDefinitionResource(Element ele) {
		// 获取 location 书序
		String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
		// 如果导入元素，location 为空，则没有导入任何资源，直接返回
		if (!StringUtils.hasText(location)) {
			getReaderContext().error("Resource location must not be empty", ele);
			return;
		}

		// Resolve system properties: e.g. "${user.dir}"
		// 使用系统变量值，解析 location 属性值
		location = getReaderContext().getEnvironment().resolveRequiredPlaceholders(location);

		Set<Resource> actualResources = new LinkedHashSet<>(4);

		// Discover whether the location is an absolute or relative URI
		// 标识，给定导入元素 location 属性值是否是绝对路径
		boolean absoluteLocation = false;
		try {
			absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
		}
		catch (URISyntaxException ex) {
			// 给定的导入元素，location 属性值不是绝对值。
			// cannot convert to an URI, considering the location relative
			// unless it is the well-known Spring prefix "classpath*:"
		}

		// Absolute or relative?
		// 给定导入元素，location 是绝对路径。
		if (absoluteLocation) {
			try {
				/**
				 * 使用资源读入器加载给定路径的 bean 资源。{@link org.springframework.beans.factory.support.AbstractBeanDefinitionReader#loadBeanDefinitions(String, Set)}
				 */
				int importCount = getReaderContext().getReader().loadBeanDefinitions(location, actualResources);
				if (logger.isTraceEnabled()) {
					logger.trace("Imported " + importCount + " bean definitions from URL location [" + location + "]");
				}
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error(
						"Failed to import bean definitions from URL location [" + location + "]", ele, ex);
			}
		}
		else {
			// No URL -> considering resource location as relative to the current file.
			// 给定导入元素，location 属性值是相对路径。
			try {
				int importCount;
				Resource relativeResource = getReaderContext().getResource().createRelative(location);
				// 相对路径资源存在。
				if (relativeResource.exists()) {
					importCount = getReaderContext().getReader().loadBeanDefinitions(relativeResource);
					actualResources.add(relativeResource);
				}
				// 封装的相对路径资源不存在。
				else {
					//  获取spring IOC 容器资源读入器的基本路径
					String baseLocation = getReaderContext().getResource().getURL().toString();

					/**
					 * 根据 spring IOC 容器，读入器基本路径加载给定导入路径的资源。{@link org.springframework.beans.factory.support.AbstractBeanDefinitionReader#loadBeanDefinitions(String, Set)}
					 */
					importCount = getReaderContext().getReader().loadBeanDefinitions(
							StringUtils.applyRelativePath(baseLocation, location), actualResources);
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Imported " + importCount + " bean definitions from relative location [" + location + "]");
				}
			}
			catch (IOException ex) {
				getReaderContext().error("Failed to resolve current resource location", ele, ex);
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error(
						"Failed to import bean definitions from relative location [" + location + "]", ele, ex);
			}
		}
		Resource[] actResArray = actualResources.toArray(new Resource[0]);
		// 解析完 <import> 元素之后，发送容器导入其他资源处理完成事件。
		getReaderContext().fireImportProcessed(location, actResArray, extractSource(ele));
	}

	/**
	 * Process the given alias element, registering the alias with the registry.
	 *
	 * 解析 <alias> 进行别名解析
	 */
	protected void processAliasRegistration(Element ele) {
		// 获取 name 属性
		String name = ele.getAttribute(NAME_ATTRIBUTE);
		// 获取 alias 属性。
		String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
		boolean valid = true;
		if (!StringUtils.hasText(name)) {
			getReaderContext().error("Name must not be empty", ele);
			valid = false;
		}
		if (!StringUtils.hasText(alias)) {
			getReaderContext().error("Alias must not be empty", ele);
			valid = false;
		}
		if (valid) {
			try {
				// 向容器的资源，读入器注册别名。
				getReaderContext().getRegistry().registerAlias(name, alias);
			}
			catch (Exception ex) {
				getReaderContext().error("Failed to register alias '" + alias +
						"' for bean with name '" + name + "'", ele, ex);
			}
			// 解析完 <alias> 之后，发送容器别名处理完成事件。
			getReaderContext().fireAliasRegistered(name, alias, extractSource(ele));
		}
	}

	/**
	 * Process the given bean element, parsing the bean definition
	 * and registering it with the registry.
	 *
	 * 解析 <bean> 资源文档对象的普通元素。
	 */
	protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
		/**
		 *  1、BeanDefinitionHolder 对 BeanDefinition 封装，即Bean 定义的封装类。
		 *  2、BeanDefinitionHolder 包含我们配置文件中各种属性，例如：class、method、id 之类的属性。
		 *
		 *  解析为 `BeanDefinitionHolder` {@link BeanDefinitionParserDelegate#parseBeanDefinitionElement(Element)}
		 */
		BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
		if (bdHolder != null) {
			bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
			try {
				// Register the final decorated instance.
				/**
				 * 向 Spring IOC 容器注册解析得到 Bean 定义，这是 bean 定义向 IOC 容器注册的入口。
				 *
				 *  对解析后的 bdHolder进行注册 {@link BeanDefinitionReaderUtils#registerBeanDefinition(BeanDefinitionHolder, BeanDefinitionRegistry)}
				 *
				 */
				BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error("Failed to register bean definition with name '" +
						bdHolder.getBeanName() + "'", ele, ex);
			}
			// Send registration event.
			/**
			 * 解析得到 Bean 定义之后，发送注册事件【 扩展方式 】Spring 中并没有对此事件进行处理。
			 */
			getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
		}
	}


	/**
	 * Allow the XML to be extensible by processing any custom element types first,
	 * before we start to process the bean definitions. This method is a natural
	 * extension point for any other custom pre-processing of the XML.
	 * <p>The default implementation is empty. Subclasses can override this method to
	 * convert custom elements into standard Spring bean definitions, for example.
	 * Implementors have access to the parser's bean definition reader and the
	 * underlying XML resource, through the corresponding accessors.
	 * @see #getReaderContext()
	 */
	protected void preProcessXml(Element root) {
	}

	/**
	 * Allow the XML to be extensible by processing any custom element types last,
	 * after we finished processing the bean definitions. This method is a natural
	 * extension point for any other custom post-processing of the XML.
	 * <p>The default implementation is empty. Subclasses can override this method to
	 * convert custom elements into standard Spring bean definitions, for example.
	 * Implementors have access to the parser's bean definition reader and the
	 * underlying XML resource, through the corresponding accessors.
	 * @see #getReaderContext()
	 */
	protected void postProcessXml(Element root) {
	}

}
