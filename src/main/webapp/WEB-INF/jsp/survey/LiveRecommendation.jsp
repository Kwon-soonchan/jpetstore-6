<%--

       Copyright 2010-2025 the original author or authors.

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

          https://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.

--%>
<%@ include file="../common/IncludeTop.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<div id="Catalog">

  <h2>AI Recommendations</h2>

  <c:if test="${!sessionScope.accountBean.authenticated}">
    <div class="Message">Please sign in to view recommendations.</div>
  </c:if>

  <c:if test="${sessionScope.accountBean.authenticated}">
    <p>
      Recommendations based on the survey answers of <strong>${actionBean.account.firstName} ${actionBean.account.lastName}</strong>.
    </p>

    <div class="Message">
      Residence environment: ${actionBean.account.residenceEnv}<br />
      Care period: ${actionBean.account.carePeriod}<br />
      Preferred color: ${actionBean.account.petColorPref}<br />
      Preferred size: ${actionBean.account.petSizePref}<br />
      Active time: ${actionBean.account.activityTime}<br />
      Diet management: ${actionBean.account.dietManagement}
    </div>

    <h3>Recommendations</h3>
    <div id="ai-rec-table-container"></div>
    <script type="application/json" id="ai-rec-data">
${actionBean.recommendationJson}
    </script>
    <script>
      (function renderRecommendations() {
        const container = document.getElementById('ai-rec-table-container');
        const dataElement = document.getElementById('ai-rec-data');
        if (!container || !dataElement) {
          return;
        }
        let recs = [];
        try {
          recs = JSON.parse(dataElement.textContent.trim());
        } catch (e) {
          container.innerHTML = '<div class="Message">Could not parse recommendations.</div>';
          return;
        }
        if (!Array.isArray(recs) || recs.length === 0) {
          container.innerHTML = '<div class="Message">No recommendations available.</div>';
          return;
        }

        const table = document.createElement('table');
        const thead = document.createElement('thead');
        const headerRow = document.createElement('tr');
        ['Category', 'Product ID', 'Product Name'].forEach(text => {
          const th = document.createElement('th');
          th.textContent = text;
          headerRow.appendChild(th);
        });
        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement('tbody');
        recs.forEach(rec => {
          const row = document.createElement('tr');
          // Derive category from productId prefix (e.g., "FI-" -> Fish)
          const category = (() => {
            if (!rec.productId) return '';
            if (rec.productId.startsWith('FI-')) return 'Fish';
            if (rec.productId.startsWith('K9-')) return 'Dogs';
            if (rec.productId.startsWith('AV-')) return 'Birds';
            if (rec.productId.startsWith('RP-')) return 'Reptiles';
            if (rec.productId.startsWith('FL-')) return 'Cats';
            return '';
          })();

          // Category column
          const catTd = document.createElement('td');
          catTd.textContent = category;
          row.appendChild(catTd);

          // Product ID + link column
          const idTd = document.createElement('td');
          if (rec.productId) {
            const link = document.createElement('a');
            link.href = '../actions/Catalog.action?viewProduct=&productId=' + encodeURIComponent(rec.productId);
            link.textContent = rec.productId;
            idTd.appendChild(link);
          }
          row.appendChild(idTd);

          // Product Name column
          ['productName'].forEach(key => {
            const td = document.createElement('td');
            td.textContent = rec[key] || '';
            row.appendChild(td);
          });
          tbody.appendChild(row);
        });
        table.appendChild(tbody);

        container.appendChild(table);
      })();
    </script>
  </c:if>

</div>

<%@ include file="../common/IncludeBottom.jsp"%>
