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
package org.mybatis.jpetstore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.jpetstore.domain.Category;
import org.mybatis.jpetstore.domain.Item;
import org.mybatis.jpetstore.domain.Product;
import org.mybatis.jpetstore.domain.SurveyRecommendation;
import org.mybatis.jpetstore.mapper.CategoryMapper;
import org.mybatis.jpetstore.mapper.ItemMapper;
import org.mybatis.jpetstore.mapper.ProductMapper;
import org.mybatis.jpetstore.mapper.SurveyRecommendationMapper;
import org.springframework.stereotype.Service;

/**
 * The Class CatalogService.
 *
 * @author Eduardo Macarron
 */
@Service
public class CatalogService {

  private final CategoryMapper categoryMapper;
  private final ItemMapper itemMapper;
  private final ProductMapper productMapper;
  private final SurveyRecommendationMapper surveyRecommendationMapper;

  public CatalogService(CategoryMapper categoryMapper, ItemMapper itemMapper, ProductMapper productMapper,
      SurveyRecommendationMapper surveyRecommendationMapper) {
    this.categoryMapper = categoryMapper;
    this.itemMapper = itemMapper;
    this.productMapper = productMapper;
    this.surveyRecommendationMapper = surveyRecommendationMapper;
  }

  public List<Category> getCategoryList() {
    return categoryMapper.getCategoryList();
  }

  public Category getCategory(String categoryId) {
    return categoryMapper.getCategory(categoryId);
  }

  public Product getProduct(String productId) {
    return productMapper.getProduct(productId);
  }

  public List<Product> getProductListByCategory(String categoryId) {
    return productMapper.getProductListByCategory(categoryId);
  }

  /**
   * Search product list.
   *
   * @param keywords
   *          the keywords
   *
   * @return the list
   */
  public List<Product> searchProductList(String keywords) {
    List<Product> products = new ArrayList<>();
    for (String keyword : keywords.split("\\s+")) {
      products.addAll(productMapper.searchProductList("%" + keyword.toLowerCase() + "%"));
    }
    return products;
  }

  public List<Item> getItemListByProduct(String productId) {
    return itemMapper.getItemListByProduct(productId);
  }

  public Item getItem(String itemId) {
    return itemMapper.getItem(itemId);
  }

  public boolean isItemInStock(String itemId) {
    return itemMapper.getInventoryQuantity(itemId) > 0;
  }

  /**
   * Check if a product is recommended for the given account based on survey preferences. A product is recommended if at
   * least 5 out of 6 survey conditions match.
   *
   * @param account
   *          the account with survey preferences
   * @param productId
   *          the product ID to check
   *
   * @return true if the product is recommended, false otherwise
   */
  public boolean isProductRecommended(org.mybatis.jpetstore.domain.Account account, String productId) {
    if (account == null || productId == null) {
      return false;
    }

    // Check if account has all required survey preferences
    String residenceEnv = account.getResidenceEnv();
    String carePeriod = account.getCarePeriod();
    String petColorPref = account.getPetColorPref();
    String petSizePref = account.getPetSizePref();
    String activityTime = account.getActivityTime();
    String dietManagement = account.getDietManagement();

    if (residenceEnv == null || residenceEnv.isEmpty() || carePeriod == null || carePeriod.isEmpty()
        || petColorPref == null || petColorPref.isEmpty() || petSizePref == null || petSizePref.isEmpty()
        || activityTime == null || activityTime.isEmpty() || dietManagement == null || dietManagement.isEmpty()) {
      return false;
    }

    try {
      // Get all survey recommendations
      List<SurveyRecommendation> allRecommendations = surveyRecommendationMapper.getSurveyRecommendations();

      ObjectMapper mapper = new ObjectMapper();

      // Check each recommendation for partial match (at least 5 out of 6 conditions)
      for (SurveyRecommendation recommendation : allRecommendations) {
        if (recommendation.getRecommendedJsonData() == null) {
          continue;
        }

        // Count matching conditions (trim strings to avoid whitespace issues)
        int matchCount = 0;
        if (residenceEnv != null && recommendation.getResidenceEnv() != null
            && residenceEnv.trim().equals(recommendation.getResidenceEnv().trim())) {
          matchCount++;
        }
        if (carePeriod != null && recommendation.getCarePeriod() != null
            && carePeriod.trim().equals(recommendation.getCarePeriod().trim())) {
          matchCount++;
        }
        if (petColorPref != null && recommendation.getPetColorPref() != null
            && petColorPref.trim().equals(recommendation.getPetColorPref().trim())) {
          matchCount++;
        }
        if (petSizePref != null && recommendation.getPetSizePref() != null
            && petSizePref.trim().equals(recommendation.getPetSizePref().trim())) {
          matchCount++;
        }
        if (activityTime != null && recommendation.getActivityTime() != null
            && activityTime.trim().equals(recommendation.getActivityTime().trim())) {
          matchCount++;
        }
        if (dietManagement != null && recommendation.getDietManagement() != null
            && dietManagement.trim().equals(recommendation.getDietManagement().trim())) {
          matchCount++;
        }

        // If at least 5 conditions match, check if productId is in recommendations
        if (matchCount >= 5) {
          JsonNode jsonArray = mapper.readTree(recommendation.getRecommendedJsonData());

          if (jsonArray.isArray()) {
            for (JsonNode node : jsonArray) {
              if (node.has("productId") && productId.equals(node.get("productId").asText())) {
                return true;
              }
            }
          }
        }
      }
    } catch (Exception e) {
      // Log error but don't break the page
      System.err.println("Error checking product recommendation: " + e.getMessage());
      e.printStackTrace();
      return false;
    }

    return false;
  }
}
