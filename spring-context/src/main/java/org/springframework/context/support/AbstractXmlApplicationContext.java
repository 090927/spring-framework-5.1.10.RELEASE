/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.context.support;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

/**
 * Convenient base class for {@link org.springframework.context.ApplicationContext}
 * implementations, drawing configuration from XML documents containing bean definitions
 * understood by an {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}.
 *
 * <p>Subclasses just have to implement the {@link #getConfigResources} and/or
 * the {@link #getConfigLocations} method. Furthermore, they might override
 * the {@link #getResourceByPath} hook to interpret relative paths in an
 * environment-specific fashion, and/or {@link #getResourcePatternResolver}
 * for extended pattern resolution.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getConfigResources
 * @see #getConfigLocations
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 */
public abstract class AbstractXmlApplicationContext extends AbstractRefreshableConfigApplicationContext {

	private boolean validating = true;


	/**
	 * Create a new AbstractXmlApplicationContext with no parent.
	 */
	public AbstractXmlApplicationContext() {
	}

	/**
	 * Create a new AbstractXmlApplicationContext with the given parent context.
	 * @param parent the parent context
	 */
	public AbstractXmlApplicationContext(@Nullable ApplicationContext parent) {
		super(parent);
	}


	/**
	 * Set whether to use XML validation. Default is {@code true}.
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}


	/**
	 * Loads the bean definitions via an XmlBeanDefinitionReader.
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
	 * @see #initBeanDefinitionReader
	 * @see #loadBeanDefinitions
	 *
	 * 【IOC 定位阶段 】载入配置路径。
	 */
	@Override
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
		// Create a new XmlBeanDefinitionReader for the given BeanFactory.
		/**
		 * 创建 XmlBeanDefinitionReader 即创建Bean 读取器。
		 * 并通过回调设置到容器。容器使用读取器读取。Bean 配置的资源。
		 */
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

		// Configure the bean definition reader with this context's
		// resource loading environment.
		/**
		 * 为Bean 读取设置 spring 资源加载器
		 * AbstractXmlApplicationContext -> （继承）AbstractApplicationContext -> DefaultResourceLoader
		 * 因此容器本身，也是一个资源加载器。
		 */
		beanDefinitionReader.setEnvironment(this.getEnvironment());
		beanDefinitionReader.setResourceLoader(this);
		// 为 bean 读取设置 SAX xml 解析器。
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

		// Allow a subclass to provide custom initialization of the reader,
		// then proceed with actually loading the bean definitions.

		/**
		 * 当Bean，读取Bean 定义的 xml  资源文件时，启用 xml 校验机制
		 */
		initBeanDefinitionReader(beanDefinitionReader);

		/**
		 * bean 读取器真正实现加载的方法 {@link #loadBeanDefinitions(XmlBeanDefinitionReader)}
		 */
		loadBeanDefinitions(beanDefinitionReader);
	}

	/**
	 * Initialize the bean definition reader used for loading the bean
	 * definitions of this context. Default implementation is empty.
	 * <p>Can be overridden in subclasses, e.g. for turning off XML validation
	 * or using a different XmlBeanDefinitionParser implementation.
	 * @param reader the bean definition reader used by this context
	 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader#setDocumentReaderClass
	 */
	protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
		reader.setValidating(this.validating);
	}

	/**
	 * Load the bean definitions with the given XmlBeanDefinitionReader.
	 * <p>The lifecycle of the bean factory is handled by the {@link #refreshBeanFactory}
	 * method; hence this method is just supposed to load and/or register bean definitions.
	 * @param reader the XmlBeanDefinitionReader to use
	 * @throws BeansException in case of bean registration errors
	 * @throws IOException if the required XML document isn't found
	 * @see #refreshBeanFactory
	 * @see #getConfigLocations
	 * @see #getResources
	 * @see #getResourcePatternResolver
	 *
	 * xml  bean 读取器加载Bean 配置资源。
	 * TODO IOC【 加载阶段 】 ~ 载入配置路径
	 */
	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {

		// 获取 bean 配置资源的定位。
		Resource[] configResources = getConfigResources();
		if (configResources != null) {
			/**
			 * xml bean 调用父类。定位bean 配置资源
			 * {@link org.springframework.beans.factory.support.AbstractBeanDefinitionReader#loadBeanDefinitions(Resource...)}
			 */
			reader.loadBeanDefinitions(configResources);
		}
		/**
		 * 如果子类中获取 bean 配置资源为空
		 * 则从 {@link ClassPathXmlApplicationContext#ClassPathXmlApplicationContext(String[], boolean, ApplicationContext)}
		 * 	 setConfigLocations 设置资源。
		 */
		String[] configLocations = getConfigLocations();
		if (configLocations != null) {

			/**
			 * 调用父类，定位Bean 配置资源
			 * {@link org.springframework.beans.factory.support.AbstractBeanDefinitionReader#loadBeanDefinitions(String...)}
			 */
			reader.loadBeanDefinitions(configLocations);
		}
	}

	/**
	 * Return an array of Resource objects, referring to the XML bean definition
	 * files that this context should be built with.
	 * <p>The default implementation returns {@code null}. Subclasses can override
	 * this to provide pre-built Resource objects rather than location Strings.
	 * @return an array of Resource objects, or {@code null} if none
	 * @see #getConfigLocations()
	 */
	@Nullable
	protected Resource[] getConfigResources() {
		return null;
	}

}
