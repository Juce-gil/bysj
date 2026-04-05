<template>
  <div class="posts-page">
    <section class="page-section publish-hero qh-panel">
      <div class="publish-hero__content">
        <div class="publish-hero__meta">
          <span class="publish-hero__eyebrow">{{ '\u6211\u7684\u53d1\u5e03\u4e2d\u5fc3' }}</span>
          <el-tag effect="dark" type="warning">{{ heroModeTag }}</el-tag>
        </div>
        <h1>{{ heroTitle }}</h1>
        <p>{{ heroDescription }}</p>
        <div class="hero-pills">
          <span class="hero-pill">{{ goodsHeroPillText }}</span>
          <span class="hero-pill">{{ wantedHeroPillText }}</span>
          <span class="hero-pill">{{ categoriesHeroPillText }}</span>
        </div>
      </div>
      <div class="publish-hero__actions">
        <el-button type="primary" @click="openGoodsCreate">{{ heroGoodsButtonText }}</el-button>
        <el-button @click="openWantedCreate">{{ heroWantedButtonText }}</el-button>
        <el-button text :loading="isRefreshingAll || categoriesLoading" @click="refreshAll">{{ refreshAllButtonText }}</el-button>
      </div>
    </section>

    <section v-if="actionFeedback" class="page-section">
      <div class="feedback-banner qh-panel" :class="`feedback-banner--${actionFeedback.tone}`">
        <div>
          <span class="feedback-banner__eyebrow">{{ '\u64cd\u4f5c\u53cd\u9988' }}</span>
          <h3>{{ actionFeedback.title }}</h3>
          <p>{{ actionFeedback.description }}</p>
        </div>
        <div class="feedback-banner__actions">
          <el-button v-if="actionFeedback.actionText && actionFeedback.actionTab" size="small" type="primary" @click="goToTab(actionFeedback.actionTab)">{{ actionFeedback.actionText }}</el-button>
          <el-button size="small" text @click="clearActionFeedback">{{ '\u77e5\u9053\u4e86' }}</el-button>
        </div>
      </div>
    </section>

    <section class="grid-cards grid-cards--4 page-section">
      <article v-for="item in stats" :key="item.label" class="stat-card qh-panel">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.tip }}</p>
      </article>
    </section>

    <SectionCard :title="sectionTitle" :subtitle="sectionSubtitle">
      <template #extra>
        <div class="section-actions">
          <el-tag effect="plain" type="warning">{{ '\u6821\u56ed\uff1a\u79d1\u6210' }}</el-tag>
          <el-tag effect="plain" type="info">{{ `\u5206\u7c7b\u6570\uff1a${categories.length}` }}</el-tag>
        </div>
      </template>
      <el-tabs v-model="activeTab" class="post-tabs">
        <el-tab-pane :label="`\u6211\u7684\u5546\u54c1\uff08${goodsList.length}\uff09`" name="goods-list">
          <div class="toolbar">
            <el-input v-model="goodsKeyword" clearable :placeholder="'\u641c\u7d22\u6211\u53d1\u5e03\u7684\u5546\u54c1\u6807\u9898\u3001\u7b80\u4ecb\u6216\u5206\u7c7b'" class="toolbar-input" />
            <el-select v-model="goodsStatusFilter" clearable :placeholder="'\u5168\u90e8\u72b6\u6001'" class="toolbar-select">
              <el-option :label="'\u5168\u90e8\u72b6\u6001'" value="" />
              <el-option v-for="item in goodsStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button text :loading="goodsLoading" @click="loadMyGoods">{{ '\u5237\u65b0\u5546\u54c1' }}</el-button>
            <el-button v-if="hasGoodsFilters" text @click="resetGoodsFilters">{{ '\u6e05\u7a7a\u7b5b\u9009' }}</el-button>
          </div>
          <div class="toolbar-summary qh-panel--subtle">
            <div><strong>{{ goodsListSummaryTitle }}</strong><p>{{ goodsListSummaryText }}</p></div>
            <el-button v-if="goodsEditingId !== null" size="small" @click="goToTab('goods-create')">{{ '\u7ee7\u7eed\u7f16\u8f91\u5546\u54c1' }}</el-button>
          </div>
          <el-skeleton v-if="goodsLoading" :rows="8" animated />
          <div v-else-if="goodsErrorMessage" class="state-panel qh-panel--subtle">
            <el-result icon="warning" :title="'\u6211\u7684\u5546\u54c1\u52a0\u8f7d\u5931\u8d25'" :sub-title="goodsErrorMessage"><template #extra><el-button type="primary" @click="loadMyGoods">{{ '\u91cd\u65b0\u52a0\u8f7d' }}</el-button></template></el-result>
          </div>
          <div v-else-if="filteredGoods.length" class="post-grid">
            <article v-for="item in filteredGoods" :key="item.id" class="post-card qh-panel--subtle" :class="{ 'post-card--editing': goodsEditingId === item.id }">
              <div class="mock-cover goods-cover" :style="{ background: getGoodsCoverBackground(item) }"><span>{{ getTitlePrefix(item.title, '\u5546\u54c1') }}</span></div>
              <div class="post-card__body">
                <div class="post-card__status-row"><div class="post-card__status-tags"><el-tag v-if="goodsEditingId === item.id" size="small" type="warning">{{ '\u6b63\u5728\u7f16\u8f91' }}</el-tag><el-tag size="small" effect="light" :type="getGoodsStatusMeta(item.status).type">{{ getGoodsStatusMeta(item.status).text }}</el-tag></div><span class="subtle-text">{{ `\u53d1\u5e03\u65f6\u95f4\uff1a${formatDateLabel(item.publishedAt)}` }}</span></div>
                <div class="post-card__head"><div class="post-card__title-block"><h3>{{ item.title }}</h3><p>{{ item.intro }}</p></div><div class="post-card__price-block"><span class="price-text">{{ formatPrice(item.price) }}</span></div></div>
                <div class="card-meta post-card__meta"><span>{{ item.category }}</span><span>{{ item.campus }}</span><span>{{ item.condition }}</span><span>{{ `\u6536\u85cf ${item.favoriteCount}` }}</span></div>
                <div v-if="item.tags.length" class="tag-row"><el-tag v-for="tag in item.tags" :key="tag" effect="plain">{{ tag }}</el-tag></div>
                <div class="post-card__footer"><span class="subtle-text">{{ goodsCardFooterText(item.status) }}</span><RouterLink :to="`/goods/${item.id}`">{{ '\u67e5\u770b\u5546\u54c1\u8be6\u60c5' }}</RouterLink></div>
                <div class="card-actions">
                  <el-button size="small" :disabled="!canEditGoods(item)" @click="startEditGoods(item)">{{ '\u4fee\u6539\u5185\u5bb9' }}</el-button>
                  <el-button v-if="canOffShelfGoods(item)" size="small" :loading="goodsActionLoadingId === item.id" @click="offShelfGoods(item)">{{ '\u6682\u65f6\u4e0b\u67b6' }}</el-button>
                  <el-button v-else-if="canRelistGoods(item)" size="small" :loading="goodsActionLoadingId === item.id" @click="relistGoods(item)">{{ '\u91cd\u65b0\u4e0a\u67b6' }}</el-button>
                  <el-button size="small" type="danger" plain :disabled="!canDeleteGoods(item)" :loading="goodsActionLoadingId === item.id" @click="removeGoods(item)">{{ '\u6c38\u4e45\u5220\u9664' }}</el-button>
                </div>
              </div>
            </article>
          </div>
          <EmptyHint v-else :title="goodsEmptyState.title" :description="goodsEmptyState.description"><template #actions><el-button v-if="hasGoodsFilters" @click="resetGoodsFilters">{{ '\u6e05\u7a7a\u7b5b\u9009' }}</el-button><el-button type="primary" @click="openGoodsCreate">{{ goodsEmptyState.actionText }}</el-button></template></EmptyHint>
        </el-tab-pane>

        <el-tab-pane :label="goodsCreateTabLabel" name="goods-create">
          <div class="publish-grid">
            <div class="publish-main qh-panel">
              <div class="form-mode-banner qh-panel--subtle"><div><span class="form-mode-banner__eyebrow">{{ goodsModeTag }}</span><h4>{{ goodsModeTitle }}</h4><p>{{ goodsModeDescription }}</p></div><el-button size="small" text @click="confirmResetGoods">{{ goodsResetButtonText }}</el-button></div>
              <div class="publish-intro"><h3>{{ goodsFormTitle }}</h3><p>{{ goodsFormDescription }}</p></div>
              <el-form ref="goodsFormRef" :model="goodsForm" :rules="goodsRules" label-position="top" class="publish-form">
                <div class="form-grid form-grid--2">
                  <el-form-item :label="'\u5546\u54c1\u6807\u9898'" prop="title"><el-input v-model="goodsForm.title" maxlength="100" show-word-limit :placeholder="'\u8bf7\u8f93\u5165\u5546\u54c1\u6807\u9898\uff0c\u4f8b\u5982\uff1a\u5bbf\u820d\u642c\u7a7a\u8f6c\u51fa\u673a\u68b0\u952e\u76d8'" /></el-form-item>
                  <el-form-item :label="'\u5546\u54c1\u5206\u7c7b'" prop="category"><el-select v-model="goodsForm.category" :placeholder="'\u8bf7\u9009\u62e9\u5546\u54c1\u5206\u7c7b'" :loading="categoriesLoading" filterable><el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.name" /></el-select></el-form-item>
                  <el-form-item :label="'\u6821\u533a / \u53d1\u5e03\u533a\u57df'" prop="campus"><el-select v-model="goodsForm.campus" :placeholder="'\u8bf7\u9009\u62e9\u53d1\u5e03\u6821\u533a'" ><el-option v-for="item in campusOptions" :key="item" :label="item" :value="item" /></el-select></el-form-item>
                  <el-form-item :label="'\u5546\u54c1\u6210\u8272'" prop="condition"><el-select v-model="goodsForm.condition" :placeholder="'\u8bf7\u9009\u62e9\u5546\u54c1\u6210\u8272'" ><el-option v-for="item in conditionOptions" :key="item" :label="item" :value="item" /></el-select></el-form-item>
                  <el-form-item :label="'\u73b0\u4ef7'" prop="price"><el-input-number v-model="goodsForm.price" :min="0.01" :precision="2" :step="1" controls-position="right" style="width: 100%;" /></el-form-item>
                  <el-form-item :label="'\u539f\u4ef7\uff08\u53ef\u9009\uff09'"><el-input-number v-model="goodsForm.originalPrice" :min="0" :precision="2" :step="1" controls-position="right" style="width: 100%;" /></el-form-item>
                </div>
                <el-form-item :label="'\u5546\u54c1\u56fe\u7247\uff08\u6700\u591a 6 \u5f20\uff09'" required>
                  <el-upload v-model:file-list="goodsImageFileList" class="goods-uploader" list-type="picture-card" :auto-upload="false" multiple :limit="MAX_GOODS_IMAGE_COUNT" accept="image/jpeg,image/png,image/webp" :before-upload="beforeGoodsImageUpload" :on-change="handleGoodsImageChange" :on-preview="handleGoodsImagePreview" :on-remove="handleGoodsImageRemove" :on-exceed="handleGoodsImageExceed">
                    <div v-if="goodsImageFileList.length < MAX_GOODS_IMAGE_COUNT" class="goods-uploader__trigger"><span class="goods-uploader__icon">+</span><strong>{{ '\u9009\u62e9\u56fe\u7247' }}</strong></div>
                  </el-upload>
                  <div class="goods-uploader__tip">{{ '\u652f\u6301 JPG / PNG / WEBP\uff0c\u5355\u5f20\u4e0d\u8d85\u8fc7 10MB\uff0c\u6700\u591a 6 \u5f20\u3002\u7f16\u8f91\u5546\u54c1\u65f6\u4f1a\u56de\u663e\u5df2\u6709\u56fe\u7247\uff0c\u53ef\u76f4\u63a5\u5220\u9664\u6216\u7ee7\u7eed\u8865\u56fe\u3002' }}</div>
                  <div class="goods-uploader__tip">{{ `\u5f53\u524d\u5df2\u9009\u62e9 ${goodsImageFileList.length} / ${MAX_GOODS_IMAGE_COUNT} \u5f20\u56fe\u7247\u3002` }}</div>
                </el-form-item>
                <el-form-item :label="'\u4e00\u53e5\u8bdd\u7b80\u4ecb'" prop="intro"><el-input v-model="goodsForm.intro" maxlength="120" show-word-limit :placeholder="'\u7528\u4e00\u53e5\u8bdd\u6982\u62ec\u5546\u54c1\u4eae\u70b9\uff0c\u4f8b\u5982\uff1a\u5bbf\u820d\u642c\u7a7a\u4f4e\u4ef7\u8f6c\u51fa'" /></el-form-item>
                <el-form-item :label="'\u8be6\u7ec6\u63cf\u8ff0'" prop="description"><el-input v-model="goodsForm.description" type="textarea" :rows="6" maxlength="1000" show-word-limit :placeholder="'\u8865\u5145\u5546\u54c1\u4f7f\u7528\u60c5\u51b5\u3001\u914d\u4ef6\u3001\u6210\u8272\u8bf4\u660e\u3001\u4ea4\u6613\u5efa\u8bae\u7b49\u4fe1\u606f'" /></el-form-item>
                <el-form-item :label="'\u5546\u54c1\u6807\u7b7e\uff08\u53ef\u9009\uff09'"><el-input v-model="goodsForm.tagsText" :placeholder="'\u591a\u4e2a\u6807\u7b7e\u8bf7\u7528\u4e2d\u6587\u9017\u53f7\u6216\u82f1\u6587\u9017\u53f7\u5206\u9694\uff0c\u4f8b\u5982\uff1a\u6559\u6750\uff0c\u6821\u5185\u9762\u4ea4\uff0c\u53ef\u5c0f\u5200'" /></el-form-item>
                <div class="form-actions"><el-button @click="confirmResetGoods">{{ goodsResetButtonText }}</el-button><el-button type="primary" :loading="goodsSubmitting" @click="submitGoods">{{ goodsSubmitButtonText }}</el-button></div>
              </el-form>
            </div>
            <aside class="publish-side qh-panel">
              <div class="draft-panel qh-panel--subtle"><div class="draft-panel__head"><span>{{ '\u5546\u54c1\u8349\u7a3f\u5b8c\u6210\u5ea6' }}</span><strong>{{ goodsDraftProgress }}%</strong></div><div class="draft-progress"><span :style="{ width: `${goodsDraftProgress}%` }"></span></div><p>{{ goodsDraftHint }}</p></div>
              <h3>{{ goodsAsideTitle }}</h3><ul><li v-for="tip in goodsAsideTips" :key="tip">{{ tip }}</li></ul>
              <div class="preview-card qh-panel--subtle"><div class="mock-cover preview-cover" :style="{ background: goodsFormPreviewBackground }">{{ getTitlePrefix(goodsForm.title, '\u5546\u54c1') }}</div><h4>{{ goodsPreviewTitle }}</h4><p>{{ goodsPreviewDescription }}</p></div>
            </aside>
          </div>
        </el-tab-pane>

        <el-tab-pane :label="`\u6211\u7684\u6c42\u8d2d\uff08${wantedList.length}\uff09`" name="wanted-list">
          <div class="toolbar"><el-input v-model="wantedKeyword" clearable :placeholder="'\u641c\u7d22\u6211\u53d1\u5e03\u7684\u6c42\u8d2d\u6807\u9898\u3001\u7b80\u4ecb\u6216\u5206\u7c7b'" class="toolbar-input" /><el-select v-model="wantedStatusFilter" clearable :placeholder="'\u5168\u90e8\u72b6\u6001'" class="toolbar-select"><el-option :label="'\u5168\u90e8\u72b6\u6001'" value="" /><el-option v-for="item in wantedStatusOptions" :key="item.value" :label="item.label" :value="item.value" /></el-select><el-button text :loading="wantedLoading" @click="loadMyWanted">{{ '\u5237\u65b0\u6c42\u8d2d' }}</el-button><el-button v-if="hasWantedFilters" text @click="resetWantedFilters">{{ '\u6e05\u7a7a\u7b5b\u9009' }}</el-button></div>
          <div class="toolbar-summary qh-panel--subtle"><div><strong>{{ wantedListSummaryTitle }}</strong><p>{{ wantedListSummaryText }}</p></div><el-button v-if="wantedEditingId !== null" size="small" @click="goToTab('wanted-create')">{{ '\u7ee7\u7eed\u7f16\u8f91\u6c42\u8d2d' }}</el-button></div>
          <el-skeleton v-if="wantedLoading" :rows="8" animated />
          <div v-else-if="wantedErrorMessage" class="state-panel qh-panel--subtle"><el-result icon="warning" :title="'\u6211\u7684\u6c42\u8d2d\u52a0\u8f7d\u5931\u8d25'" :sub-title="wantedErrorMessage"><template #extra><el-button type="primary" @click="loadMyWanted">{{ '\u91cd\u65b0\u52a0\u8f7d' }}</el-button></template></el-result></div>
          <div v-else-if="filteredWanted.length" class="post-grid">
            <article v-for="item in filteredWanted" :key="item.id" class="post-card qh-panel--subtle" :class="{ 'post-card--editing': wantedEditingId === item.id }">
              <div class="mock-cover wanted-cover" :style="{ background: item.coverStyle }"><span>{{ getTitlePrefix(item.title, '\u6c42\u8d2d') }}</span></div>
              <div class="post-card__body">
                <div class="post-card__status-row"><div class="post-card__status-tags"><el-tag v-if="wantedEditingId === item.id" size="small" type="warning">{{ '\u6b63\u5728\u7f16\u8f91' }}</el-tag><el-tag size="small" effect="light" :type="getWantedStatusMeta(item.status).type">{{ getWantedStatusMeta(item.status).text }}</el-tag></div><span class="subtle-text">{{ `\u622a\u6b62\uff1a${item.deadline || '\u672a\u586b\u5199'}` }}</span></div>
                <div class="post-card__head"><div class="post-card__title-block"><h3>{{ item.title }}</h3><p>{{ item.intro }}</p></div><div class="post-card__price-block"><span class="budget-text">{{ item.budget }}</span></div></div>
                <div class="card-meta post-card__meta"><span>{{ item.category }}</span><span>{{ item.campus }}</span><span>{{ wantedCardFooterText(item.status) }}</span></div>
                <div v-if="item.tags.length" class="tag-row"><el-tag v-for="tag in item.tags" :key="tag" effect="plain">{{ tag }}</el-tag></div>
                <div class="contact-row"><span>{{ '\u8054\u7cfb\u65b9\u5f0f\uff1a' }}</span><span>{{ formatContact(item.phone, item.qq, item.contactVisible) }}</span></div>
                <div class="post-card__footer"><span class="subtle-text">{{ wantedCardHintText(item.status) }}</span><RouterLink :to="`/wanted/${item.id}`">{{ '\u67e5\u770b\u6c42\u8d2d\u8be6\u60c5' }}</RouterLink></div>
                <div class="card-actions"><el-button size="small" :disabled="!canEditWanted(item)" @click="startEditWanted(item)">{{ '\u4fee\u6539\u9700\u6c42' }}</el-button><el-button v-if="canCloseWanted(item)" size="small" :loading="wantedActionLoadingId === item.id" @click="closeWanted(item)">{{ '\u5173\u95ed\u6c42\u8d2d' }}</el-button><el-button v-else-if="canReopenWanted(item)" size="small" :loading="wantedActionLoadingId === item.id" @click="reopenWanted(item)">{{ '\u91cd\u65b0\u5f00\u653e' }}</el-button><el-button size="small" type="danger" plain :loading="wantedActionLoadingId === item.id" @click="removeWanted(item)">{{ '\u6c38\u4e45\u5220\u9664' }}</el-button></div>
              </div>
            </article>
          </div>
          <EmptyHint v-else :title="wantedEmptyState.title" :description="wantedEmptyState.description"><template #actions><el-button v-if="hasWantedFilters" @click="resetWantedFilters">{{ '\u6e05\u7a7a\u7b5b\u9009' }}</el-button><el-button type="primary" @click="openWantedCreate">{{ wantedEmptyState.actionText }}</el-button></template></EmptyHint>
        </el-tab-pane>

        <el-tab-pane :label="wantedCreateTabLabel" name="wanted-create">
          <div class="publish-grid">
            <div class="publish-main qh-panel">
              <div class="form-mode-banner qh-panel--subtle"><div><span class="form-mode-banner__eyebrow">{{ wantedModeTag }}</span><h4>{{ wantedModeTitle }}</h4><p>{{ wantedModeDescription }}</p></div><el-button size="small" text @click="confirmResetWanted">{{ wantedResetButtonText }}</el-button></div>
              <div class="publish-intro"><h3>{{ wantedFormTitle }}</h3><p>{{ wantedFormDescription }}</p></div>
              <el-form ref="wantedFormRef" :model="wantedForm" :rules="wantedRules" label-position="top" class="publish-form">
                <div class="form-grid form-grid--2"><el-form-item :label="'\u6c42\u8d2d\u6807\u9898'" prop="title"><el-input v-model="wantedForm.title" maxlength="100" show-word-limit :placeholder="'\u4f8b\u5982\uff1a\u6c42\u8d2d\u4e8c\u624b\u9ad8\u6570\u6559\u6750'" /></el-form-item><el-form-item :label="'\u6c42\u8d2d\u5206\u7c7b'" prop="category"><el-select v-model="wantedForm.category" :placeholder="'\u8bf7\u9009\u62e9\u6c42\u8d2d\u5206\u7c7b'" :loading="categoriesLoading" filterable><el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.name" /></el-select></el-form-item><el-form-item :label="'\u6821\u533a / \u9762\u4ea4\u8303\u56f4'" prop="campus"><el-select v-model="wantedForm.campus" :placeholder="'\u8bf7\u9009\u62e9\u6821\u533a\u6216\u533a\u57df'" ><el-option v-for="item in campusOptions" :key="item" :label="item" :value="item" /></el-select></el-form-item><el-form-item :label="'\u9884\u7b97\u8bf4\u660e'" prop="budget"><el-input v-model="wantedForm.budget" maxlength="30" :placeholder="'\u4f8b\u5982\uff1a\u9884\u7b97 30 \u5143\u5de6\u53f3'" /></el-form-item><el-form-item :label="'\u622a\u6b62\u65e5\u671f'" prop="deadline"><el-date-picker v-model="wantedForm.deadline" type="date" value-format="YYYY-MM-DD" :placeholder="'\u8bf7\u9009\u62e9\u622a\u6b62\u65e5\u671f'" style="width: 100%;" /></el-form-item></div>
                <el-form-item :label="'\u4e00\u53e5\u8bdd\u7b80\u4ecb'" prop="intro"><el-input v-model="wantedForm.intro" maxlength="120" show-word-limit :placeholder="'\u4f8b\u5982\uff1a\u60f3\u6536\u4e00\u672c\u5185\u5bb9\u5b8c\u6574\u3001\u7b14\u8bb0\u8f83\u5c11\u7684\u6559\u6750\u3002'" /></el-form-item>
                <el-form-item :label="'\u8be6\u7ec6\u9700\u6c42'" prop="description"><el-input v-model="wantedForm.description" type="textarea" :rows="6" maxlength="1000" show-word-limit :placeholder="'\u53ef\u586b\u5199\u9884\u7b97\u3001\u6210\u8272\u9884\u671f\u3001\u662f\u5426\u9700\u8981\u914d\u4ef6\u3001\u662f\u5426\u6025\u9700\u7b49\u3002'" /></el-form-item>
                <el-form-item :label="'\u6807\u7b7e\uff08\u53ef\u9009\uff09'"><el-input v-model="wantedForm.tagsText" :placeholder="'\u591a\u4e2a\u6807\u7b7e\u8bf7\u7528\u4e2d\u6587\u9017\u53f7\u6216\u82f1\u6587\u9017\u53f7\u5206\u9694\uff0c\u4f8b\u5982\uff1a\u6559\u6750\uff0c\u5907\u8003\uff0c\u6821\u5185\u9762\u4ea4'" /></el-form-item>
                <div class="form-actions"><el-button @click="confirmResetWanted">{{ wantedResetButtonText }}</el-button><el-button type="primary" :loading="wantedSubmitting" @click="submitWanted">{{ wantedSubmitButtonText }}</el-button></div>
              </el-form>
            </div>
            <aside class="publish-side qh-panel"><div class="draft-panel qh-panel--subtle"><div class="draft-panel__head"><span>{{ '\u6c42\u8d2d\u8349\u7a3f\u5b8c\u6210\u5ea6' }}</span><strong>{{ wantedDraftProgress }}%</strong></div><div class="draft-progress"><span :style="{ width: `${wantedDraftProgress}%` }"></span></div><p>{{ wantedDraftHint }}</p></div><h3>{{ wantedAsideTitle }}</h3><ul><li v-for="tip in wantedAsideTips" :key="tip">{{ tip }}</li></ul><div class="preview-card qh-panel--subtle"><div class="mock-cover preview-cover preview-cover--wanted">{{ getTitlePrefix(wantedForm.title, '\u6c42\u8d2d') }}</div><h4>{{ wantedPreviewTitle }}</h4><p>{{ wantedPreviewDescription }}</p></div></aside>
          </div>
        </el-tab-pane>
      </el-tabs>
      <el-dialog v-model="goodsImagePreviewVisible" :title="'\u56fe\u7247\u9884\u89c8'" width="min(92vw, 960px)"><img v-if="goodsImagePreviewUrl" :src="goodsImagePreviewUrl" :alt="'\u5546\u54c1\u56fe\u7247\u9884\u89c8'" class="goods-image-preview-dialog__image" /></el-dialog>
    </SectionCard>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules, UploadFile, UploadProps, UploadUserFile } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { RouterLink } from 'vue-router';
import EmptyHint from '@/components/EmptyHint.vue';
import SectionCard from '@/components/SectionCard.vue';
import {
  closeWantedPost,
  createGoodsPost,
  createWantedPost,
  deleteGoodsPost,
  deleteWantedPost,
  getCategories,
  getMyGoodsList,
  getMyWantedList,
  offShelfGoodsPost,
  relistGoodsPost,
  reopenWantedPost,
  updateGoodsPost,
  updateWantedPost,
  uploadGoodsImages,
  type CategoryItem,
  type CreateGoodsPayload,
  type CreateWantedPayload,
  type GoodsItem,
  type WantedItem,
} from '@/api/marketplace';
import { formatDate, formatPrice } from '@/utils/format';
import { getGoodsStatusMeta, getWantedStatusMeta } from '@/utils/status';

const MAX_GOODS_IMAGE_COUNT = 6;
const MAX_GOODS_IMAGE_SIZE_MB = 10;

type TabKey = 'goods-list' | 'goods-create' | 'wanted-list' | 'wanted-create';
type FeedbackTone = 'success' | 'warning' | 'info';

type ActionFeedback = {
  tone: FeedbackTone;
  title: string;
  description: string;
  actionText?: string;
  actionTab?: TabKey;
};

const activeTab = ref<TabKey>('goods-list');
const isRefreshingAll = ref(false);
const actionFeedback = ref<ActionFeedback | null>(null);
const categories = ref<CategoryItem[]>([]);
const categoriesLoading = ref(false);

const goodsList = ref<GoodsItem[]>([]);
const goodsLoading = ref(false);
const goodsSubmitting = ref(false);
const goodsErrorMessage = ref('');
const goodsKeyword = ref('');
const goodsStatusFilter = ref('');
const goodsActionLoadingId = ref<number | null>(null);
const goodsEditingId = ref<number | null>(null);

const wantedList = ref<WantedItem[]>([]);
const wantedLoading = ref(false);
const wantedSubmitting = ref(false);
const wantedErrorMessage = ref('');
const wantedKeyword = ref('');
const wantedStatusFilter = ref('');
const wantedActionLoadingId = ref<number | null>(null);
const wantedEditingId = ref<number | null>(null);

const goodsImageFileList = ref<UploadUserFile[]>([]);
const goodsImagePreviewVisible = ref(false);
const goodsImagePreviewUrl = ref('');
const goodsPreviewBlobUrls = new Set<string>();

const campusOptions = ['\u79d1\u6210\u4e3b\u6821\u533a', '\u79d1\u6210\u6559\u5b66\u533a', '\u79d1\u6210\u56fe\u4e66\u9986\u533a', '\u79d1\u6210\u5bbf\u820d\u533a', '\u79d1\u6210\u4f53\u80b2\u9986', '\u79d1\u6210\u64cd\u573a\u533a'];
const conditionOptions = ['\u5168\u65b0', '\u4e5d\u6210\u65b0', '\u516b\u6210\u65b0', '\u4e03\u6210\u65b0\u53ca\u4ee5\u4e0b'];

const goodsFormRef = ref<FormInstance>();
const wantedFormRef = ref<FormInstance>();

const goodsForm = reactive({
  title: '',
  category: '',
  campus: campusOptions[0],
  condition: conditionOptions[1],
  price: undefined as number | undefined,
  originalPrice: undefined as number | undefined,
  intro: '',
  description: '',
  tagsText: '',
});

const wantedForm = reactive({
  title: '',
  category: '',
  campus: campusOptions[0],
  budget: '',
  deadline: '',
  intro: '',
  description: '',
  tagsText: '',
});

const goodsRules: FormRules<typeof goodsForm> = {
  title: [{ required: true, message: '\u8bf7\u8f93\u5165\u5546\u54c1\u6807\u9898', trigger: 'blur' }],
  category: [{ required: true, message: '\u8bf7\u9009\u62e9\u5546\u54c1\u5206\u7c7b', trigger: 'change' }],
  campus: [{ required: true, message: '\u8bf7\u9009\u62e9\u6821\u533a', trigger: 'change' }],
  condition: [{ required: true, message: '\u8bf7\u9009\u62e9\u5546\u54c1\u6210\u8272', trigger: 'change' }],
  price: [{ required: true, message: '\u8bf7\u8f93\u5165\u5546\u54c1\u73b0\u4ef7', trigger: 'change' }],
  intro: [{ required: true, message: '\u8bf7\u8f93\u5165\u4e00\u53e5\u8bdd\u7b80\u4ecb', trigger: 'blur' }],
  description: [{ required: true, message: '\u8bf7\u8f93\u5165\u8be6\u7ec6\u63cf\u8ff0', trigger: 'blur' }],
};

const wantedRules: FormRules<typeof wantedForm> = {
  title: [{ required: true, message: '\u8bf7\u8f93\u5165\u6c42\u8d2d\u6807\u9898', trigger: 'blur' }],
  category: [{ required: true, message: '\u8bf7\u9009\u62e9\u6c42\u8d2d\u5206\u7c7b', trigger: 'change' }],
  campus: [{ required: true, message: '\u8bf7\u9009\u62e9\u6821\u533a', trigger: 'change' }],
  budget: [{ required: true, message: '\u8bf7\u8f93\u5165\u9884\u7b97\u8bf4\u660e', trigger: 'blur' }],
  deadline: [{ required: true, message: '\u8bf7\u9009\u62e9\u622a\u6b62\u65e5\u671f', trigger: 'change' }],
  intro: [{ required: true, message: '\u8bf7\u8f93\u5165\u4e00\u53e5\u8bdd\u7b80\u4ecb', trigger: 'blur' }],
  description: [{ required: true, message: '\u8bf7\u8f93\u5165\u8be6\u7ec6\u9700\u6c42', trigger: 'blur' }],
};
const normalizeStatus = (status: string) => status.trim().toLowerCase();
const isGoodsOnSaleStatus = (status: string) => ['on_sale', 'selling'].includes(normalizeStatus(status));
const isGoodsOffShelfStatus = (status: string) => ['off_shelf', 'offsale', 'removed'].includes(normalizeStatus(status));
const isGoodsReservedStatus = (status: string) => ['reserved', 'booked'].includes(normalizeStatus(status));
const isGoodsFinishedStatus = (status: string) => ['sold', 'finished'].includes(normalizeStatus(status));
const isWantedOpenStatus = (status: string) => ['buying', 'open', 'active'].includes(normalizeStatus(status));
const isWantedClosedStatus = (status: string) => ['closed', 'cancelled', 'expired'].includes(normalizeStatus(status));
const isWantedFinishedStatus = (status: string) => ['finished', 'completed', 'matched'].includes(normalizeStatus(status));

const goodsStatusOptions = computed(() => ['on_sale', 'reserved', 'sold', 'off_shelf'].map((value) => ({ value, label: getGoodsStatusMeta(value).text })));
const wantedStatusOptions = computed(() => ['buying', 'finished', 'closed'].map((value) => ({ value, label: getWantedStatusMeta(value).text })));

const filteredGoods = computed(() => {
  const keyword = goodsKeyword.value.trim().toLowerCase();
  return goodsList.value.filter((item) => {
    const hitKeyword = !keyword || `${item.title}${item.intro}${item.description}${item.category}`.toLowerCase().includes(keyword);
    const hitStatus = !goodsStatusFilter.value || normalizeStatus(item.status) === goodsStatusFilter.value;
    return hitKeyword && hitStatus;
  });
});

const filteredWanted = computed(() => {
  const keyword = wantedKeyword.value.trim().toLowerCase();
  return wantedList.value.filter((item) => {
    const hitKeyword = !keyword || `${item.title}${item.intro}${item.description}${item.category}`.toLowerCase().includes(keyword);
    const hitStatus = !wantedStatusFilter.value || normalizeStatus(item.status) === wantedStatusFilter.value;
    return hitKeyword && hitStatus;
  });
});

const stats = computed(() => [
  { label: '\u6211\u7684\u5546\u54c1', value: String(goodsList.value.length).padStart(2, '0'), tip: '\u5df2\u53d1\u5e03\u5546\u54c1\u603b\u6570' },
  { label: '\u5728\u552e\u5546\u54c1', value: String(goodsList.value.filter((item) => isGoodsOnSaleStatus(item.status)).length).padStart(2, '0'), tip: '\u5f53\u524d\u4ecd\u5728\u552e\u4e2d\u7684\u5546\u54c1\u6570' },
  { label: '\u6211\u7684\u6c42\u8d2d', value: String(wantedList.value.length).padStart(2, '0'), tip: '\u5df2\u53d1\u5e03\u6c42\u8d2d\u603b\u6570' },
  { label: '\u5f00\u653e\u6c42\u8d2d', value: String(wantedList.value.filter((item) => isWantedOpenStatus(item.status)).length).padStart(2, '0'), tip: '\u5f53\u524d\u4ecd\u5728\u5f00\u653e\u4e2d\u7684\u6c42\u8d2d\u6570' },
]);

const hasGoodsFilters = computed(() => Boolean(goodsKeyword.value.trim() || goodsStatusFilter.value));
const hasWantedFilters = computed(() => Boolean(wantedKeyword.value.trim() || wantedStatusFilter.value));
const goodsEditingItem = computed(() => goodsList.value.find((item) => item.id === goodsEditingId.value) || null);
const wantedEditingItem = computed(() => wantedList.value.find((item) => item.id === wantedEditingId.value) || null);

const hasGoodsDraft = computed(() => Boolean(goodsForm.title.trim() || goodsForm.category || goodsForm.campus || goodsForm.condition || goodsForm.price != null || goodsForm.originalPrice != null || goodsForm.intro.trim() || goodsForm.description.trim() || goodsForm.tagsText.trim() || goodsImageFileList.value.length));
const hasWantedDraft = computed(() => Boolean(wantedForm.title.trim() || wantedForm.category || wantedForm.campus || wantedForm.budget.trim() || wantedForm.deadline || wantedForm.intro.trim() || wantedForm.description.trim() || wantedForm.tagsText.trim()));

const goodsDraftProgress = computed(() => {
  const filled = [goodsForm.title.trim(), goodsForm.category, goodsForm.campus, goodsForm.condition, goodsForm.price != null, goodsForm.intro.trim(), goodsForm.description.trim()].filter(Boolean).length;
  return Math.round((filled / 7) * 100);
});
const wantedDraftProgress = computed(() => {
  const filled = [wantedForm.title.trim(), wantedForm.category, wantedForm.campus, wantedForm.budget.trim(), wantedForm.deadline, wantedForm.intro.trim(), wantedForm.description.trim()].filter(Boolean).length;
  return Math.round((filled / 7) * 100);
});

const heroModeTag = computed(() => {
  if (activeTab.value === 'goods-create') return goodsEditingId.value !== null ? '\u5546\u54c1\u7f16\u8f91\u4e2d' : '\u53d1\u5e03\u5546\u54c1';
  if (activeTab.value === 'wanted-create') return wantedEditingId.value !== null ? '\u6c42\u8d2d\u7f16\u8f91\u4e2d' : '\u53d1\u5e03\u6c42\u8d2d';
  return activeTab.value === 'wanted-list' ? '\u7ba1\u7406\u6211\u7684\u6c42\u8d2d' : '\u7ba1\u7406\u6211\u7684\u5546\u54c1';
});
const heroTitle = computed(() => {
  if (activeTab.value === 'goods-create') return goodsEditingId.value !== null ? '\u6b63\u5728\u4fee\u6539\u5546\u54c1\uff0c\u4fdd\u5b58\u540e\u4f1a\u81ea\u52a8\u56de\u5230\u5217\u8868' : '\u53d1\u5e03\u4e00\u4ef6\u95f2\u7f6e\uff0c\u8ba9\u540c\u5b66\u66f4\u5feb\u627e\u5230\u4f60';
  if (activeTab.value === 'wanted-create') return wantedEditingId.value !== null ? '\u6b63\u5728\u4fee\u6539\u6c42\u8d2d\u9700\u6c42\uff0c\u4fdd\u6301\u4fe1\u606f\u65b0\u9c9c\u66f4\u5bb9\u6613\u6210\u4ea4' : '\u628a\u9700\u6c42\u8bf4\u6e05\u695a\uff0c\u8ba9\u5408\u9002\u7684\u4eba\u4e3b\u52a8\u8054\u7cfb\u4f60';
  if (activeTab.value === 'wanted-list') return '\u7ba1\u7406\u6211\u7684\u6c42\u8d2d\uff0c\u72b6\u6001\u4e0e\u9700\u6c42\u4e00\u773c\u770b\u6e05';
  return '\u7ba1\u7406\u6211\u7684\u5546\u54c1\uff0c\u53d1\u5e03\u3001\u7f16\u8f91\u3001\u4e0a\u4e0b\u67b6\u4e00\u9875\u5b8c\u6210';
});
const heroDescription = computed(() => {
  if (activeTab.value === 'goods-create') return goodsEditingId.value !== null ? `\u4f60\u6b63\u5728\u7f16\u8f91${goodsEditingItem.value ? `\u300a${goodsEditingItem.value.title}\u300b` : '\u5f53\u524d\u5546\u54c1'}\uff0c\u4fdd\u5b58\u540e\u4f1a\u81ea\u52a8\u56de\u5230\u201c\u6211\u7684\u5546\u54c1\u201d\uff0c\u65b9\u4fbf\u7ee7\u7eed\u68c0\u67e5\u72b6\u6001\u3002` : '\u8865\u9f50\u6807\u9898\u3001\u4ef7\u683c\u3001\u56fe\u7247\u4e0e\u63cf\u8ff0\u540e\uff0c\u5546\u54c1\u4f1a\u7acb\u523b\u8fdb\u5165\u5546\u54c1\u5e7f\u573a\uff0c\u5e76\u540c\u6b65\u5230\u201c\u6211\u7684\u5546\u54c1\u201d\u4e2d\u3002';
  if (activeTab.value === 'wanted-create') return wantedEditingId.value !== null ? `\u4f60\u6b63\u5728\u7f16\u8f91${wantedEditingItem.value ? `\u300a${wantedEditingItem.value.title}\u300b` : '\u5f53\u524d\u6c42\u8d2d'}\uff0c\u4fdd\u5b58\u540e\u4f1a\u81ea\u52a8\u56de\u5230\u201c\u6211\u7684\u6c42\u8d2d\u201d\uff0c\u65b9\u4fbf\u7ee7\u7eed\u7ba1\u7406\u3002` : '\u5199\u6e05\u9884\u7b97\u3001\u622a\u6b62\u65f6\u95f4\u548c\u4ea4\u6613\u8303\u56f4\u540e\uff0c\u6c42\u8d2d\u4f1a\u7acb\u523b\u8fdb\u5165\u6c42\u8d2d\u5927\u5385\uff0c\u5e76\u540c\u6b65\u5230\u201c\u6211\u7684\u6c42\u8d2d\u201d\u4e2d\u3002';
  if (activeTab.value === 'wanted-list') return '\u652f\u6301\u641c\u7d22\u3001\u5173\u95ed\u6c42\u8d2d\u3001\u91cd\u65b0\u5f00\u653e\u4e0e\u5220\u9664\uff0c\u8ba9\u9700\u6c42\u7ba1\u7406\u66f4\u987a\u624b\u3002';
  return '\u4fdd\u7559\u73b0\u6709\u63a5\u53e3\u80fd\u529b\uff0c\u53ea\u6536\u53e3\u524d\u7aef\u6587\u6848\u3001\u63d0\u793a\u3001\u53cd\u9988\u4e0e\u5371\u9669\u64cd\u4f5c\u786e\u8ba4\uff0c\u4fdd\u6301\u9ec4\u9ed1\u98ce\u683c\u4e00\u81f4\u3002';
});
const goodsHeroPillText = computed(() => `\u5546\u54c1 ${goodsList.value.length} \u6761`);
const wantedHeroPillText = computed(() => `\u6c42\u8d2d ${wantedList.value.length} \u6761`);
const categoriesHeroPillText = computed(() => `\u5206\u7c7b ${categories.value.length} \u4e2a`);
const heroGoodsButtonText = computed(() => goodsEditingId.value !== null ? '\u7ee7\u7eed\u7f16\u8f91\u5546\u54c1' : '\u53bb\u53d1\u5e03\u5546\u54c1');
const heroWantedButtonText = computed(() => wantedEditingId.value !== null ? '\u7ee7\u7eed\u7f16\u8f91\u6c42\u8d2d' : '\u53bb\u53d1\u5e03\u6c42\u8d2d');
const refreshAllButtonText = computed(() => (isRefreshingAll.value || categoriesLoading.value) ? '\u6b63\u5728\u540c\u6b65...' : '\u5237\u65b0\u5217\u8868\u4e0e\u5206\u7c7b');
const sectionTitle = computed(() => activeTab.value === 'goods-create' ? (goodsEditingId.value !== null ? '\u7f16\u8f91\u5546\u54c1\u4fe1\u606f' : '\u53d1\u5e03\u65b0\u5546\u54c1') : activeTab.value === 'wanted-create' ? (wantedEditingId.value !== null ? '\u7f16\u8f91\u6c42\u8d2d\u4fe1\u606f' : '\u53d1\u5e03\u65b0\u6c42\u8d2d') : '\u53d1\u5e03\u7ba1\u7406');
const sectionSubtitle = computed(() => activeTab.value === 'goods-create' ? (goodsEditingId.value !== null ? '\u7f16\u8f91\u6001\u4f1a\u4fdd\u7559\u539f\u5546\u54c1\u5185\u5bb9\uff0c\u4fdd\u5b58\u540e\u81ea\u52a8\u8fd4\u56de\u201c\u6211\u7684\u5546\u54c1\u201d\u3002' : '\u53d1\u5e03\u5546\u54c1\u524d\u53ef\u5148\u770b\u8349\u7a3f\u5b8c\u6210\u5ea6\u3001\u9884\u89c8\u5361\u7247\u4e0e\u56fe\u7247\u56de\u663e\u3002') : activeTab.value === 'wanted-create' ? (wantedEditingId.value !== null ? '\u7f16\u8f91\u6001\u4f1a\u4fdd\u7559\u539f\u6c42\u8d2d\u5185\u5bb9\uff0c\u4fdd\u5b58\u540e\u81ea\u52a8\u8fd4\u56de\u201c\u6211\u7684\u6c42\u8d2d\u201d\u3002' : '\u53d1\u5e03\u6c42\u8d2d\u524d\u5efa\u8bae\u5148\u786e\u8ba4\u9884\u7b97\u3001\u622a\u6b62\u65e5\u671f\u4e0e\u9762\u4ea4\u8303\u56f4\u3002') : '\u7edf\u4e00\u7ba1\u7406\u5546\u54c1\u4e0e\u6c42\u8d2d\uff0c\u652f\u6301\u7b5b\u9009\u3001\u7f16\u8f91\u3001\u4e0a\u4e0b\u67b6\u3001\u5173\u95ed\u4e0e\u5220\u9664\u7b49\u64cd\u4f5c\u3002');
