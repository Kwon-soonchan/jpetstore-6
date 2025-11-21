/*
 *    Copyright 2010-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.jpetstore.web.actions;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.integration.spring.SpringBean;

import org.mybatis.jpetstore.domain.Account;
import org.mybatis.jpetstore.domain.Category;
import org.mybatis.jpetstore.domain.Product;
import org.mybatis.jpetstore.service.CatalogService;
import org.mybatis.jpetstore.service.OpenAiRecommendationService;

/**
 * ActionBean that triggers a live AI-based recommendation using the user's survey answers.
 */
public class LiveRecommendationActionBean extends AbstractActionBean {

  private static final long serialVersionUID = -4105627783175794034L;

  private static final String VIEW = "/WEB-INF/jsp/survey/LiveRecommendation.jsp";

  @SpringBean
  private transient CatalogService catalogService;

  @SpringBean
  private transient OpenAiRecommendationService openAiRecommendationService;

  private Account account;
  private String recommendationJson;

  public Account getAccount() {
    return account;
  }

  public String getRecommendationJson() {
    return recommendationJson;
  }

  @DefaultHandler
  public Resolution showRecommendation() {
    HttpSession session = context.getRequest().getSession(false);
    AccountActionBean accountBean = session == null ? null : (AccountActionBean) session.getAttribute("accountBean");

    if (accountBean == null || !accountBean.isAuthenticated()) {
      setMessage("Please sign in before requesting a recommendation.");
      return new RedirectResolution(AccountActionBean.class);
    }

    account = accountBean.getAccount();

    List<Product> products = new ArrayList<>();
    List<Category> categories = catalogService.getCategoryList();
    for (Category category : categories) {
      products.addAll(catalogService.getProductListByCategory(category.getCategoryId()));
    }

    recommendationJson = openAiRecommendationService.getRecommendation(account, products);
    return new ForwardResolution(VIEW);
  }
}
