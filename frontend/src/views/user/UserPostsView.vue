<template>
  <div class="posts-page">
    <section class="overview-banner qh-panel">
      <div class="overview-banner__main">
        <span class="overview-badge">MY POSTS CENTER</span>
        <h1>我的发布</h1>
        <p>集中查看商品与求购发布记录，让用户端与管理端形成前后台联动的完整展示链路。</p>
      </div>
      <div class="overview-banner__actions">
        <el-button type="primary" @click="openGoodsCreate">发布商品</el-button>
        <el-button @click="openWantedCreate">发布求购</el-button>
        <el-button text :loading="refreshing" @click="refreshAll">刷新全部</el-button>
      </div>
    </section>

    <section class="stats grid-cards grid-cards--4">
      <article class="stat-card qh-panel"><span>我的商品</span><strong>{{ goodsList.length }}</strong><p>当前已发布商品记录数</p></article>
      <article class="stat-card qh-panel"><span>在售商品</span><strong>{{ goodsList.filter((item) => canOffShelfGoods(item.status)).length }}</strong><p>支持继续展示与交易沟通</p></article>
      <article class="stat-card qh-panel"><span>我的求购</span><strong>{{ wantedList.length }}</strong><p>当前已发布求购记录数</p></article>
      <article class="stat-card qh-panel"><span>开放求购</span><strong>{{ wantedList.filter((item) => canCloseWanted(item.status)).length }}</strong><p>仍在持续征集的求购需求</p></article>
    </section>

    <section class="board qh-panel">
      <div class="board__head">
        <div>
          <h3>发布管理工作台</h3>
          <p>统一查看与维护商品、求购内容，支持筛选、编辑、删除和真实接口提交。</p>
        </div>
        <div class="board__tags">
          <el-tag effect="plain">默认校区：{{ defaultCampus }}</el-tag>
          <el-tag effect="plain" type="success">真实接口</el-tag>
        </div>
      </div>

      <div class="board-summary qh-panel--subtle">
        <article>
          <span>当前标签页</span>
          <strong>{{ activeTab === 'goods' ? '商品管理' : '求购管理' }}</strong>
          <p>{{ activeTab === 'goods' ? `当前匹配 ${filteredGoods.length} 条商品` : `当前匹配 ${filteredWanted.length} 条求购` }}</p>
        </article>
        <article>
          <span>筛选关键字</span>
          <strong>{{ activeTab === 'goods' ? (goodsKeyword || '未设置') : (wantedKeyword || '未设置') }}</strong>
          <p>可结合状态筛选快速定位已发布内容</p>
        </article>
        <article>
          <span>快捷操作</span>
          <strong>{{ activeTab === 'goods' ? '商品发布 / 编辑' : '求购发布 / 编辑' }}</strong>
          <p>支持图片上传、详情跳转与状态管理</p>
        </article>
      </div>

      <el-tabs v-model="activeTab" class="tabs">
        <el-tab-pane :label="`我的商品（${goodsList.length}）`" name="goods">
          <div class="toolbar qh-panel--subtle">
            <el-input v-model="goodsKeyword" clearable class="toolbar__input" placeholder="搜索标题、简介、分类、标签" />
            <el-select v-model="goodsStatusFilter" clearable class="toolbar__select" placeholder="全部状态">
              <el-option label="全部状态" value="" />
              <el-option v-for="item in goodsStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button type="primary" plain @click="openGoodsCreate">发布商品</el-button>
            <el-button :loading="goodsLoading" @click="loadMyGoods">刷新</el-button>
          </div>
          <el-skeleton v-if="goodsLoading" :rows="6" animated />
          <el-result v-else-if="goodsError" icon="warning" title="商品列表加载失败" :sub-title="goodsError" />
          <el-empty v-else-if="!filteredGoods.length" description="暂无符合条件的商品发布" />
          <div v-else class="table-shell">
            <el-table :data="filteredGoods" stripe>
              <el-table-column label="商品" min-width="280">
                <template #default="{ row }"><div class="title-cell"><strong>{{ row.title }}</strong><p>{{ row.intro || '暂无简介' }}</p></div></template>
              </el-table-column>
              <el-table-column label="价格" min-width="120"><template #default="{ row }">{{ formatPrice(row.price) }}</template></el-table-column>
              <el-table-column prop="category" label="分类" min-width="120" />
              <el-table-column prop="campus" label="校区" min-width="120" />
              <el-table-column label="状态" min-width="120"><template #default="{ row }"><el-tag size="small" effect="light" :type="getGoodsStatusMeta(row.status).type">{{ getGoodsStatusMeta(row.status).text }}</el-tag></template></el-table-column>
              <el-table-column label="操作" min-width="320" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="goGoodsDetail(row.id)">详情</el-button>
                  <el-button link type="primary" @click="startEditGoods(row)">编辑</el-button>
                  <el-button v-if="canOffShelfGoods(row.status)" link type="warning" :loading="goodsAct(row.id, 'off')" @click="offGoods(row)">下架</el-button>
                  <el-button v-else-if="canRelistGoods(row.status)" link type="success" :loading="goodsAct(row.id, 'relist')" @click="relistGoods(row)">重新上架</el-button>
                  <el-button link type="danger" :loading="goodsAct(row.id, 'delete')" @click="deleteGoods(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane :label="`我的求购（${wantedList.length}）`" name="wanted">
          <div class="toolbar qh-panel--subtle">
            <el-input v-model="wantedKeyword" clearable class="toolbar__input" placeholder="搜索标题、简介、分类、标签" />
            <el-select v-model="wantedStatusFilter" clearable class="toolbar__select" placeholder="全部状态">
              <el-option label="全部状态" value="" />
              <el-option v-for="item in wantedStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button type="primary" plain @click="openWantedCreate">发布求购</el-button>
            <el-button :loading="wantedLoading" @click="loadMyWanted">刷新</el-button>
          </div>
          <el-skeleton v-if="wantedLoading" :rows="6" animated />
          <el-result v-else-if="wantedError" icon="warning" title="求购列表加载失败" :sub-title="wantedError" />
          <el-empty v-else-if="!filteredWanted.length" description="暂无符合条件的求购发布" />
          <div v-else class="table-shell">
            <el-table :data="filteredWanted" stripe>
              <el-table-column label="求购" min-width="280"><template #default="{ row }"><div class="title-cell"><strong>{{ row.title }}</strong><p>{{ row.intro || '暂无简介' }}</p></div></template></el-table-column>
              <el-table-column prop="budget" label="预算" min-width="140" />
              <el-table-column prop="category" label="分类" min-width="120" />
              <el-table-column prop="campus" label="校区" min-width="120" />
              <el-table-column label="状态" min-width="120"><template #default="{ row }"><el-tag size="small" effect="light" :type="getWantedStatusMeta(row.status).type">{{ getWantedStatusMeta(row.status).text }}</el-tag></template></el-table-column>
              <el-table-column label="操作" min-width="320" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="goWantedDetail(row.id)">详情</el-button>
                  <el-button link type="primary" @click="startEditWanted(row)">编辑</el-button>
                  <el-button v-if="canCloseWanted(row.status)" link type="warning" :loading="wantedAct(row.id, 'close')" @click="closeWanted(row)">关闭</el-button>
                  <el-button v-else-if="canReopenWanted(row.status)" link type="success" :loading="wantedAct(row.id, 'reopen')" @click="reopenWanted(row)">重新开放</el-button>
                  <el-button link type="danger" :loading="wantedAct(row.id, 'delete')" @click="deleteWantedItem(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>

    <el-dialog v-model="goodsDialog" width="960px" top="4vh" class="publish-dialog publish-dialog--goods" destroy-on-close>
      <template #header>
        <div class="publish-header">
          <div class="publish-header__badge">SELLING STUDIO</div>
          <div class="publish-header__content">
            <div>
              <h2>{{ goodsEditingId ? '编辑商品信息' : '发布闲置商品' }}</h2>
              <p>换成更清爽的卡片式发布面板，突出价格、图片和商品亮点，让发布效率与质感一起提升。</p>
            </div>
            <div class="publish-header__meta">
              <span><el-icon><GoodsFilled /></el-icon>{{ goodsEditingId ? '修改已有内容' : '新建商品卡片' }}</span>
              <span><el-icon><PictureFilled /></el-icon>最多 {{ MAX_IMAGE_COUNT }} 张图片</span>
            </div>
          </div>
        </div>
      </template>

      <div class="publish-shell">
        <aside class="publish-side">
          <div class="publish-side__card publish-side__card--hero">
            <span class="publish-side__label">发布预览</span>
            <strong>{{ goodsForm.title.trim() || '未命名商品' }}</strong>
            <p>{{ goodsForm.intro.trim() || '一句话简介会展示在列表卡片里，建议突出品牌、型号或使用情况。' }}</p>
            <div class="publish-side__price">
              <span>预估展示价</span>
              <strong>{{ goodsForm.price ? `¥ ${Number(goodsForm.price).toFixed(2)}` : '待填写' }}</strong>
            </div>
            <div class="publish-progress">
              <div class="publish-progress__head">
                <span>完成度</span>
                <strong>{{ goodsPublishCompletion }}%</strong>
              </div>
              <el-progress :percentage="goodsPublishCompletion" :show-text="false" :stroke-width="10" />
            </div>
          </div>

          <div class="publish-side__metrics">
            <article>
              <el-icon><CollectionTag /></el-icon>
              <div>
                <strong>{{ goodsForm.category || '待选分类' }}</strong>
                <span>商品分类</span>
              </div>
            </article>
            <article>
              <el-icon><School /></el-icon>
              <div>
                <strong>{{ goodsForm.campus || defaultCampus }}</strong>
                <span>发布校区</span>
              </div>
            </article>
            <article>
              <el-icon><PriceTag /></el-icon>
              <div>
                <strong>{{ tags(goodsForm.tagsText).length }}/8</strong>
                <span>标签数量</span>
              </div>
            </article>
          </div>

          <div class="publish-side__card">
            <span class="publish-side__label">发布建议</span>
            <ul class="publish-checklist">
              <li>标题里带上品牌 / 型号 / 关键词，更容易被搜索到。</li>
              <li>简介建议 20~50 字，先说核心卖点，再补充成色。</li>
              <li>图片优先上传封面、细节、瑕疵图，提升信任感。</li>
            </ul>
            <div v-if="goodsMissingFields.length" class="publish-side__todo">
              <strong>待补充</strong>
              <div class="publish-chip-list">
                <span v-for="item in goodsMissingFields" :key="item" class="publish-chip">{{ item }}</span>
              </div>
            </div>
          </div>
        </aside>

        <el-form ref="goodsFormRef" :model="goodsForm" :rules="goodsRules" label-position="top" status-icon class="dialog-form dialog-form--fancy">
          <section class="form-section">
            <div class="form-section__head">
              <div>
                <span class="form-section__eyebrow">Basic Info</span>
                <h3>基础信息</h3>
              </div>
              <p>先把商品的核心信息补全，买家会在列表页最先看到这些内容。</p>
            </div>
            <div class="form-grid form-grid--2">
              <el-form-item label="标题">
                <el-input v-model="goodsForm.title" maxlength="100" show-word-limit placeholder="例如：95 新 iPad Air 5 代 64G 蓝色" />
              </el-form-item>
              <el-form-item label="分类">
                <el-select v-model="goodsForm.category" filterable placeholder="请选择商品分类">
                  <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </div>
            <div class="form-grid form-grid--3">
              <el-form-item label="售价">
                <el-input-number v-model="goodsForm.price" :min="0.01" :precision="2" class="full-width" placeholder="期望售价" />
              </el-form-item>
              <el-form-item label="原价">
                <el-input-number v-model="goodsForm.originalPrice" :min="0" :precision="2" class="full-width" placeholder="可选" />
              </el-form-item>
              <el-form-item label="成色">
                <el-select v-model="goodsForm.condition" filterable allow-create default-first-option placeholder="选择商品成色">
                  <el-option v-for="item in conditionOptions" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </div>
          </section>

          <section class="form-section">
            <div class="form-section__head">
              <div>
                <span class="form-section__eyebrow">Exposure Setup</span>
                <h3>曝光信息</h3>
              </div>
              <p>通过校区、标签和简介强化筛选命中率，让商品更容易被快速发现。</p>
            </div>
            <div class="form-grid form-grid--2">
              <el-form-item label="校区">
                <el-select v-model="goodsForm.campus" filterable allow-create default-first-option placeholder="选择发布校区">
                  <el-option v-for="item in campusOptions" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
              <el-form-item label="标签">
                <el-input v-model="goodsForm.tagsText" placeholder="多个标签可用空格或中文逗号分隔" />
              </el-form-item>
            </div>
            <div v-if="goodsTagsPreview.length" class="tag-preview">
              <span class="tag-preview__label">标签预览</span>
              <div class="tag-preview__list">
                <el-tag v-for="item in goodsTagsPreview" :key="item" size="small" effect="plain">{{ item }}</el-tag>
              </div>
            </div>
            <el-form-item label="简介">
              <el-input v-model="goodsForm.intro" maxlength="120" show-word-limit placeholder="一句话说明商品亮点、使用情况或交易优势" />
            </el-form-item>
            <el-form-item label="详细描述">
              <el-input v-model="goodsForm.description" type="textarea" :rows="6" maxlength="1000" show-word-limit placeholder="建议补充购买时间、使用频率、配件清单、是否支持议价等信息" />
            </el-form-item>
          </section>

          <section class="form-section">
            <div class="form-section__head">
              <div>
                <span class="form-section__eyebrow">Visual Assets</span>
                <h3>图片素材</h3>
              </div>
              <p>封面图决定第一印象，建议至少上传 3 张，包含整体图、细节图和瑕疵图。</p>
            </div>
            <div class="upload-panel">
              <div class="upload-panel__tips">
                <span>当前已上传 {{ goodsFiles.length }} / {{ MAX_IMAGE_COUNT }}</span>
                <p>{{ goodsUploadAdvice }}</p>
              </div>
              <div v-if="goodsFiles.length" class="cover-manager">
                <div class="cover-manager__hero">
                  <img :src="goodsCoverFile?.url" alt="商品封面" class="cover-manager__hero-image" />
                  <div class="cover-manager__hero-badge">当前封面</div>
                  <div class="cover-manager__hero-info">
                    <strong>{{ goodsCoverFile?.name || '封面图片' }}</strong>
                    <span>列表页将优先展示这张图片，建议使用最清晰、最能体现商品全貌的主图。</span>
                  </div>
                </div>
                <div class="cover-manager__grid">
                  <article v-for="(file, index) in goodsFiles" :key="`${file.uid}-${index}`" class="cover-card" :class="{ 'cover-card--active': index === 0, 'cover-card--dragging': dragState.goods === index }" draggable="true" @dragstart="handleDragStart('goods', index)" @dragover="handleDragOver" @drop="handleDrop('goods', index)" @dragend="clearDragState('goods')">
                    <div class="cover-card__media">
                      <img :src="file.url" :alt="file.name" />
                      <span v-if="index === 0" class="cover-card__flag">封面</span>
                      <span class="cover-card__drag-hint"><el-icon><Rank /></el-icon>拖拽排序</span>
                      <div class="cover-card__overlay">
                        <el-button circle text bg @click="previewUpload(file)"><el-icon><ZoomIn /></el-icon></el-button>
                        <el-button circle text bg :disabled="index === 0" @click="setCoverImage('goods', index)"><el-icon><StarFilled /></el-icon></el-button>
                        <el-button circle text bg :disabled="index === 0" @click="moveInList('goods', index, -1)"><el-icon><ArrowLeft /></el-icon></el-button>
                        <el-button circle text bg :disabled="index === goodsFiles.length - 1" @click="moveInList('goods', index, 1)"><el-icon><ArrowRight /></el-icon></el-button>
                        <el-button circle text bg type="danger" @click="removeVisualImage('goods', index)"><el-icon><Delete /></el-icon></el-button>
                      </div>
                    </div>
                    <div class="cover-card__meta">
                      <strong>{{ file.name }}</strong>
                      <span>第 {{ index + 1 }} 张{{ index === 0 ? ' · 封面' : '' }}</span>
                    </div>
                  </article>
                </div>
              </div>
              <el-upload v-model:file-list="goodsFiles" list-type="picture-card" accept="image/*" :auto-upload="false" :limit="MAX_IMAGE_COUNT" :before-upload="beforeImageUpload" :on-change="onGoodsUploadChange" :on-remove="onGoodsUploadRemove" :on-preview="onPreview" :on-exceed="onExceed">
                <div class="upload-trigger">
                  <strong>+</strong>
                  <span>上传图片</span>
                  <small>建议首图做封面</small>
                </div>
              </el-upload>
              <el-alert v-if="goodsFiles.length" type="success" :closable="false" class="upload-panel__alert" show-icon title="当前默认使用第 1 张图片作为封面展示，请把最清晰的主图放在最前面。" />
            </div>
            <el-form-item prop="imageCount" class="upload-validator"><div class="upload-validator__placeholder"></div></el-form-item>
          </section>
        </el-form>
      </div>

      <template #footer>
        <div class="publish-footer">
          <div class="publish-footer__hint">
            <strong>{{ goodsEditingId ? '编辑模式' : '发布模式' }}</strong>
            <span>{{ goodsEditingId ? '保存后会同步刷新我的商品列表。' : '发布后商品将进入前台展示并支持后续管理。' }}</span>
          </div>
          <div class="publish-footer__actions">
            <el-button @click="closeGoodsDialog">取消</el-button>
            <el-button type="primary" :loading="goodsSubmitting" @click="submitGoods">{{ goodsEditingId ? '保存商品' : '立即发布' }}</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="wantedDialog" width="960px" top="4vh" class="publish-dialog publish-dialog--wanted" destroy-on-close>
      <template #header>
        <div class="publish-header publish-header--wanted">
          <div class="publish-header__badge">BUYING STUDIO</div>
          <div class="publish-header__content">
            <div>
              <h2>{{ wantedEditingId ? '编辑求购需求' : '发布求购信息' }}</h2>
              <p>保持与商品发布一致的工作台风格，把预算、截止时间和需求亮点组织得更清晰，提升整站一致性。</p>
            </div>
            <div class="publish-header__meta">
              <span><el-icon><CollectionTag /></el-icon>{{ wantedEditingId ? '调整需求内容' : '创建新求购单' }}</span>
              <span><el-icon><Calendar /></el-icon>{{ wantedForm.deadline || '设置截止日期' }}</span>
            </div>
          </div>
        </div>
      </template>

      <div class="publish-shell">
        <aside class="publish-side">
          <div class="publish-side__card publish-side__card--hero publish-side__card--wanted">
            <span class="publish-side__label">需求预览</span>
            <strong>{{ wantedForm.title.trim() || '未命名求购需求' }}</strong>
            <p>{{ wantedForm.intro.trim() || '一句话概括你的需求，例如目标型号、期望成色或交易范围。' }}</p>
            <div class="publish-side__price publish-side__price--wanted">
              <span>预算展示</span>
              <strong>{{ wantedForm.budget.trim() || '待填写' }}</strong>
            </div>
            <div class="publish-progress">
              <div class="publish-progress__head">
                <span>完成度</span>
                <strong>{{ wantedPublishCompletion }}%</strong>
              </div>
              <el-progress :percentage="wantedPublishCompletion" :show-text="false" :stroke-width="10" />
            </div>
          </div>

          <div class="publish-side__metrics">
            <article>
              <el-icon><CollectionTag /></el-icon>
              <div>
                <strong>{{ wantedForm.category || '待选分类' }}</strong>
                <span>需求分类</span>
              </div>
            </article>
            <article>
              <el-icon><School /></el-icon>
              <div>
                <strong>{{ wantedForm.campus || defaultCampus }}</strong>
                <span>交易校区</span>
              </div>
            </article>
            <article>
              <el-icon><Calendar /></el-icon>
              <div>
                <strong>{{ wantedForm.deadline || '未设置' }}</strong>
                <span>截止日期</span>
              </div>
            </article>
          </div>

          <div class="publish-side__card">
            <span class="publish-side__label">填写建议</span>
            <ul class="publish-checklist">
              <li>标题建议写清楚目标商品、预算区间和成色要求。</li>
              <li>简介先写最关注的诉求，例如品牌、型号、配置或数量。</li>
              <li>详细描述可补充验货方式、交易偏好和截止时间说明。</li>
            </ul>
            <div v-if="wantedMissingFields.length" class="publish-side__todo">
              <strong>待补充</strong>
              <div class="publish-chip-list">
                <span v-for="item in wantedMissingFields" :key="item" class="publish-chip">{{ item }}</span>
              </div>
            </div>
          </div>
        </aside>

        <el-form ref="wantedFormRef" :model="wantedForm" :rules="wantedRules" label-position="top" status-icon class="dialog-form dialog-form--fancy">
          <section class="form-section">
            <div class="form-section__head">
              <div>
                <span class="form-section__eyebrow">Basic Info</span>
                <h3>需求概况</h3>
              </div>
              <p>先明确你想买什么、预算多少以及想在什么时候前收到合适回复。</p>
            </div>
            <div class="form-grid form-grid--2">
              <el-form-item label="标题">
                <el-input v-model="wantedForm.title" maxlength="100" show-word-limit placeholder="例如：求购一台 8 成新以上的 Kindle Paperwhite" />
              </el-form-item>
              <el-form-item label="分类">
                <el-select v-model="wantedForm.category" filterable placeholder="请选择求购分类">
                  <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </div>
            <div class="form-grid form-grid--2">
              <el-form-item label="预算">
                <el-input v-model="wantedForm.budget" maxlength="30" show-word-limit placeholder="例如：500-700 元 / 预算 600 左右" />
              </el-form-item>
              <el-form-item label="截止日期">
                <el-date-picker v-model="wantedForm.deadline" type="date" value-format="YYYY-MM-DD" class="full-width" placeholder="选择截止日期" />
              </el-form-item>
            </div>
          </section>

          <section class="form-section">
            <div class="form-section__head">
              <div>
                <span class="form-section__eyebrow">Exposure Setup</span>
                <h3>筛选与曝光</h3>
              </div>
              <p>补充校区、标签和一句话简介，方便卖家快速判断是否能满足你的需求。</p>
            </div>
            <div class="form-grid form-grid--2">
              <el-form-item label="校区">
                <el-select v-model="wantedForm.campus" filterable allow-create default-first-option placeholder="选择交易校区">
                  <el-option v-for="item in campusOptions" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
              <el-form-item label="标签">
                <el-input v-model="wantedForm.tagsText" placeholder="多个标签可用空格或中文逗号分隔" />
              </el-form-item>
            </div>
            <div v-if="wantedTagsPreview.length" class="tag-preview">
              <span class="tag-preview__label">标签预览</span>
              <div class="tag-preview__list">
                <el-tag v-for="item in wantedTagsPreview" :key="item" size="small" effect="plain">{{ item }}</el-tag>
              </div>
            </div>
            <el-form-item label="简介">
              <el-input v-model="wantedForm.intro" maxlength="120" show-word-limit placeholder="一句话总结你的目标需求、预算重点或优先条件" />
            </el-form-item>
            <el-form-item label="详细描述">
              <el-input v-model="wantedForm.description" type="textarea" :rows="6" maxlength="1000" show-word-limit placeholder="建议写明品牌偏好、功能要求、交易方式、验货要求等补充信息" />
            </el-form-item>
          </section>

          <section class="form-section">
            <div class="form-section__head">
              <div>
                <span class="form-section__eyebrow">Visual Assets</span>
                <h3>参考图片</h3>
              </div>
              <p>如果你有参考款式、目标型号或示意图，可以上传图片帮助卖家更准确理解需求。</p>
            </div>
            <div class="upload-panel upload-panel--wanted">
              <div class="upload-panel__tips">
                <span>当前已上传 {{ wantedFiles.length }} / {{ MAX_IMAGE_COUNT }}</span>
                <p>{{ wantedUploadAdvice }}</p>
              </div>
              <div v-if="wantedFiles.length" class="cover-manager">
                <div class="cover-manager__hero">
                  <img :src="wantedCoverFile?.url" alt="求购参考封面" class="cover-manager__hero-image" />
                  <div class="cover-manager__hero-badge">当前主参考图</div>
                  <div class="cover-manager__hero-info">
                    <strong>{{ wantedCoverFile?.name || '参考图片' }}</strong>
                    <span>如果有多张参考图，列表会优先展示第 1 张，建议放最能表达需求的示意图。</span>
                  </div>
                </div>
                <div class="cover-manager__grid">
                  <article v-for="(file, index) in wantedFiles" :key="`${file.uid}-${index}`" class="cover-card" :class="{ 'cover-card--active': index === 0, 'cover-card--dragging': dragState.wanted === index }" draggable="true" @dragstart="handleDragStart('wanted', index)" @dragover="handleDragOver" @drop="handleDrop('wanted', index)" @dragend="clearDragState('wanted')">
                    <div class="cover-card__media">
                      <img :src="file.url" :alt="file.name" />
                      <span v-if="index === 0" class="cover-card__flag">主图</span>
                      <span class="cover-card__drag-hint"><el-icon><Rank /></el-icon>拖拽排序</span>
                      <div class="cover-card__overlay">
                        <el-button circle text bg @click="previewUpload(file)"><el-icon><ZoomIn /></el-icon></el-button>
                        <el-button circle text bg :disabled="index === 0" @click="setCoverImage('wanted', index)"><el-icon><StarFilled /></el-icon></el-button>
                        <el-button circle text bg :disabled="index === 0" @click="moveInList('wanted', index, -1)"><el-icon><ArrowLeft /></el-icon></el-button>
                        <el-button circle text bg :disabled="index === wantedFiles.length - 1" @click="moveInList('wanted', index, 1)"><el-icon><ArrowRight /></el-icon></el-button>
                        <el-button circle text bg type="danger" @click="removeVisualImage('wanted', index)"><el-icon><Delete /></el-icon></el-button>
                      </div>
                    </div>
                    <div class="cover-card__meta">
                      <strong>{{ file.name }}</strong>
                      <span>第 {{ index + 1 }} 张{{ index === 0 ? ' · 主图' : '' }}</span>
                    </div>
                  </article>
                </div>
              </div>
              <el-upload v-model:file-list="wantedFiles" list-type="picture-card" accept="image/*" :auto-upload="false" :limit="MAX_IMAGE_COUNT" :before-upload="beforeImageUpload" :on-change="onWantedUploadChange" :on-remove="onWantedUploadRemove" :on-preview="onPreview" :on-exceed="onExceed">
                <div class="upload-trigger">
                  <strong>+</strong>
                  <span>上传图片</span>
                  <small>可选参考图</small>
                </div>
              </el-upload>
            </div>
          </section>
        </el-form>
      </div>

      <template #footer>
        <div class="publish-footer">
          <div class="publish-footer__hint">
            <strong>{{ wantedEditingId ? '编辑模式' : '发布模式' }}</strong>
            <span>{{ wantedEditingId ? '保存后会同步刷新我的求购列表。' : '发布后求购需求将立即进入展示与筛选列表。' }}</span>
          </div>
          <div class="publish-footer__actions">
            <el-button @click="closeWantedDialog">取消</el-button>
            <el-button type="primary" :loading="wantedSubmitting" @click="submitWanted">{{ wantedEditingId ? '保存求购' : '立即发布' }}</el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="图片预览" width="720px"><img v-if="previewUrl" :src="previewUrl" alt="preview" class="preview-image" /></el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadFile, type UploadFiles, type UploadProps, type UploadRawFile, type UploadUserFile } from 'element-plus';
import { ArrowLeft, ArrowRight, Calendar, CollectionTag, Delete, GoodsFilled, PictureFilled, PriceTag, Rank, School, StarFilled, ZoomIn } from '@element-plus/icons-vue';
import { closeWantedPost, createGoodsPost, createWantedPost, deleteGoodsPost, deleteWantedPost, getCategories, getMyGoodsList, getMyWantedList, offShelfGoodsPost, relistGoodsPost, reopenWantedPost, updateGoodsPost, updateWantedPost, uploadGoodsImages, type CategoryItem, type CreateGoodsPayload, type CreateWantedPayload, type GoodsItem, type WantedItem } from '@/api/marketplace';
import { useUserStore } from '@/stores/user';
import { formatPrice } from '@/utils/format';
import { getGoodsStatusMeta, getWantedStatusMeta } from '@/utils/status';

type TabName = 'goods' | 'wanted';
type UploadBucket = 'goods' | 'wanted';
type GoodsForm = { title: string; price: number | undefined; originalPrice: number | undefined; category: string; campus: string; condition: string; intro: string; description: string; tagsText: string; imageCount: number };
type WantedForm = { title: string; budget: string; category: string; campus: string; deadline: string; intro: string; description: string; tagsText: string };

const MAX_IMAGE_COUNT = 6, GOODS_OFF = new Set(['off_shelf', 'offsale', 'removed']), GOODS_LOCK = new Set(['sold', 'finished']), WANTED_CLOSED = new Set(['closed', 'cancelled', 'expired']), WANTED_DONE = new Set(['finished', 'completed', 'matched']);
const CATEGORY_ALIAS_MAP: Record<string, string> = { 数码: '数码产品', 书籍: '书籍教材', 生活用品: '日用品', 服饰: '衣物鞋帽', 运动: '体育用品', 其他: '其他' };
const FALLBACK_CATEGORY_OPTIONS = Object.values(CATEGORY_ALIAS_MAP);
const normalizeCategory = (value: string) => {
  const normalized = value.trim();
  return CATEGORY_ALIAS_MAP[normalized] || normalized;
};
const router = useRouter(), userStore = useUserStore();
const activeTab = ref<TabName>('goods'), refreshing = ref(false), goodsLoading = ref(false), wantedLoading = ref(false), goodsSubmitting = ref(false), wantedSubmitting = ref(false), goodsError = ref(''), wantedError = ref(''), goodsKeyword = ref(''), wantedKeyword = ref(''), goodsStatusFilter = ref(''), wantedStatusFilter = ref(''), goodsDialog = ref(false), wantedDialog = ref(false), previewVisible = ref(false), previewUrl = ref(''), goodsEditingId = ref<number | null>(null), wantedEditingId = ref<number | null>(null), goodsActionId = ref<number | null>(null), goodsActionType = ref(''), wantedActionId = ref<number | null>(null), wantedActionType = ref('');
const categories = ref<CategoryItem[]>([]), goodsList = ref<GoodsItem[]>([]), wantedList = ref<WantedItem[]>([]), goodsFiles = ref<UploadUserFile[]>([]), wantedFiles = ref<UploadUserFile[]>([]);
const dragState = reactive<{ goods: number | null; wanted: number | null }>({ goods: null, wanted: null });
const goodsFormRef = ref<FormInstance>(), wantedFormRef = ref<FormInstance>();
const defaultCampus = computed(() => userStore.profile?.school || '科成');
const makeGoodsForm = (): GoodsForm => ({ title: '', price: undefined, originalPrice: undefined, category: '', campus: defaultCampus.value, condition: '全新', intro: '', description: '', tagsText: '', imageCount: 0 });
const makeWantedForm = (): WantedForm => ({ title: '', budget: '', category: '', campus: defaultCampus.value, deadline: '', intro: '', description: '', tagsText: '' });
const goodsForm = reactive<GoodsForm>(makeGoodsForm()), wantedForm = reactive<WantedForm>(makeWantedForm());
const previewMap = new Map<number, string>();
const categoryOptions = computed(() => Array.from(new Set([
  ...(categories.value.length ? categories.value.map((item) => item.name) : FALLBACK_CATEGORY_OPTIONS),
  ...goodsList.value.map((item) => item.category),
  ...wantedList.value.map((item) => item.category),
].map((item) => normalizeCategory(item)).filter(Boolean))));
const campusOptions = computed(() => Array.from(new Set([defaultCampus.value, '科成', ...goodsList.value.map((item) => item.campus), ...wantedList.value.map((item) => item.campus)].filter(Boolean))));
const conditionOptions = computed(() => Array.from(new Set(['全新', '九成新', '八成新', '七成新', '正常使用', ...goodsList.value.map((item) => item.condition)].filter(Boolean))));
const goodsStatusOptions = computed(() => Array.from(new Set(goodsList.value.map((item) => item.status).filter(Boolean))).map((value) => ({ value, label: getGoodsStatusMeta(value).text })));
const wantedStatusOptions = computed(() => Array.from(new Set(wantedList.value.map((item) => item.status).filter(Boolean))).map((value) => ({ value, label: getWantedStatusMeta(value).text })));
const goodsTagsPreview = computed(() => tags(goodsForm.tagsText));
const wantedTagsPreview = computed(() => tags(wantedForm.tagsText));
const goodsCoverFile = computed(() => goodsFiles.value[0] || null);
const wantedCoverFile = computed(() => wantedFiles.value[0] || null);
const goodsPublishCompletion = computed(() => {
  const checks = [Boolean(goodsForm.title.trim()), goodsForm.price != null && Number(goodsForm.price) > 0, Boolean(goodsForm.category.trim()), Boolean(goodsForm.campus.trim()), Boolean(goodsForm.condition.trim()), Boolean(goodsForm.intro.trim()), Boolean(goodsForm.description.trim()), goodsFiles.value.length > 0];
  return Math.round((checks.filter(Boolean).length / checks.length) * 100);
});
const wantedPublishCompletion = computed(() => {
  const checks = [Boolean(wantedForm.title.trim()), Boolean(wantedForm.budget.trim()), Boolean(wantedForm.category.trim()), Boolean(wantedForm.campus.trim()), Boolean(wantedForm.deadline.trim()), Boolean(wantedForm.intro.trim()), Boolean(wantedForm.description.trim())];
  return Math.round((checks.filter(Boolean).length / checks.length) * 100);
});
const goodsMissingFields = computed(() => [
  !goodsForm.title.trim() ? '补充商品标题' : '',
  goodsForm.price == null || Number(goodsForm.price) <= 0 ? '设置合理售价' : '',
  !goodsForm.category.trim() ? '选择商品分类' : '',
  !goodsForm.campus.trim() ? '选择发布校区' : '',
  !goodsForm.condition.trim() ? '补充商品成色' : '',
  !goodsForm.intro.trim() ? '填写一句话简介' : '',
  !goodsForm.description.trim() ? '完善详细描述' : '',
  goodsFiles.value.length === 0 ? '上传至少 1 张图片' : '',
].filter(Boolean));
const wantedMissingFields = computed(() => [
  !wantedForm.title.trim() ? '补充求购标题' : '',
  !wantedForm.budget.trim() ? '填写预算说明' : '',
  !wantedForm.category.trim() ? '选择求购分类' : '',
  !wantedForm.campus.trim() ? '选择交易校区' : '',
  !wantedForm.deadline.trim() ? '设置截止日期' : '',
  !wantedForm.intro.trim() ? '填写一句话简介' : '',
  !wantedForm.description.trim() ? '完善详细描述' : '',
].filter(Boolean));
const goodsUploadAdvice = computed(() => goodsFiles.value.length ? (goodsFiles.value.length < 3 ? '建议继续补充细节图和瑕疵图，提高买家信任度。' : '图片数量不错，建议将最清晰的主图放在第一张。') : '建议至少上传 1 张主图，最好包含整体图和细节图。');
const wantedUploadAdvice = computed(() => wantedFiles.value.length ? '已添加参考图片，卖家更容易理解你的目标款式。' : '求购图片不是必填，但参考图能显著提升沟通效率。');
const hit = (parts: string[], keyword: string) => !keyword.trim() || parts.join(' ').toLowerCase().includes(keyword.trim().toLowerCase());
const filteredGoods = computed(() => goodsList.value.filter((item) => hit([item.title, item.intro, item.description, item.category, item.tags.join(' ')], goodsKeyword.value) && (!goodsStatusFilter.value || item.status === goodsStatusFilter.value)));
const filteredWanted = computed(() => wantedList.value.filter((item) => hit([item.title, item.intro, item.description, item.category, item.tags.join(' ')], wantedKeyword.value) && (!wantedStatusFilter.value || item.status === wantedStatusFilter.value)));
watch(categoryOptions, (value) => { if (!goodsEditingId.value && !goodsForm.category && value.length) goodsForm.category = value[0]; if (!wantedEditingId.value && !wantedForm.category && value.length) wantedForm.category = value[0]; }, { immediate: true });
watch(conditionOptions, (value) => { if (!goodsEditingId.value && !goodsForm.condition && value.length) goodsForm.condition = value[0]; }, { immediate: true });
watch(defaultCampus, (value) => { if (!goodsEditingId.value && !goodsForm.campus) goodsForm.campus = value; if (!wantedEditingId.value && !wantedForm.campus) wantedForm.campus = value; }, { immediate: true });
watch(goodsFiles, (files) => { goodsForm.imageCount = files.length; if (goodsDialog.value) nextTick(() => goodsFormRef.value?.validateField('imageCount').catch(() => undefined)); }, { deep: true, immediate: true });
const goodsRules: FormRules<GoodsForm> = {
  title: [{ required: true, message: '请输入商品标题', trigger: 'blur' }],
  price: [{ required: true, validator: (_rule, value, callback) => (value != null && Number(value) > 0 ? callback() : callback(new Error('请输入正确的商品售价'))), trigger: 'change' }],
  category: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  campus: [{ required: true, message: '请选择校区', trigger: 'change' }],
  condition: [{ required: true, message: '请选择商品成色', trigger: 'change' }],
  intro: [{ required: true, message: '请输入商品简介', trigger: 'blur' }],
  description: [{ required: true, message: '请输入商品详细描述', trigger: 'blur' }],
  imageCount: [{ required: true, validator: (_rule, value, callback) => (Number(value) > 0 ? callback() : callback(new Error('请至少上传一张商品图片'))), trigger: 'change' }],
};
const wantedRules: FormRules<WantedForm> = {
  title: [{ required: true, message: '请输入求购标题', trigger: 'blur' }],
  budget: [{ required: true, message: '请输入预算说明', trigger: 'blur' }],
  category: [{ required: true, message: '请选择求购分类', trigger: 'change' }],
  campus: [{ required: true, message: '请选择校区', trigger: 'change' }],
  deadline: [{ required: true, message: '请选择截止日期', trigger: 'change' }],
  intro: [{ required: true, message: '请输入求购简介', trigger: 'blur' }],
  description: [{ required: true, message: '请输入求购详细描述', trigger: 'blur' }],
};
const statusKey = (value: string) => value.trim().toLowerCase();
const canOffShelfGoods = (status: string) => !GOODS_OFF.has(statusKey(status)) && !GOODS_LOCK.has(statusKey(status));
const canRelistGoods = (status: string) => GOODS_OFF.has(statusKey(status));
const canCloseWanted = (status: string) => !WANTED_CLOSED.has(statusKey(status)) && !WANTED_DONE.has(statusKey(status));
const canReopenWanted = (status: string) => WANTED_CLOSED.has(statusKey(status));
const err = (error: unknown, fallback: string) => typeof error === 'object' && error ? ((error as { response?: { data?: { message?: string } }; message?: string }).response?.data?.message || (error as { message?: string }).message || fallback) : fallback;
const tags = (source: string) => Array.from(new Set(source.split(/[，,、\n\s]+/).map((item) => item.trim()).filter(Boolean))).slice(0, 8);
const uidNum = (value: number | string | undefined) => { const next = Number(value); return Number.isFinite(next) ? next : null; };
const ensureUrl = (uid: number, raw?: UploadRawFile) => { if (!raw) return ''; const current = previewMap.get(uid); if (current) return current; const next = URL.createObjectURL(raw); previewMap.set(uid, next); return next; };
const releaseUrl = (uid: number | string | undefined) => { const key = uidNum(uid); if (key == null) return; const current = previewMap.get(key); if (current) { URL.revokeObjectURL(current); previewMap.delete(key); } };
const normFile = (file: UploadFile | UploadUserFile, index: number): UploadUserFile => { const uid = uidNum(file.uid) ?? (Date.now() + index); const raw = file.raw as UploadRawFile | undefined; const url = typeof file.url === 'string' && file.url ? file.url : ensureUrl(uid, raw); return { ...file, uid, name: String(file.name || `图片${index + 1}`), status: file.status ?? 'success', url: String(url || ''), raw }; };
const normList = (files: Array<UploadFile | UploadUserFile>) => files.slice(0, MAX_IMAGE_COUNT).map(normFile);
const remoteList = (urls: string[]) => urls.filter(Boolean).slice(0, MAX_IMAGE_COUNT).map((url, index) => ({ uid: Date.now() + index, name: `图片${index + 1}`, status: 'success' as const, url: String(url) }));
const clearList = (list: UploadUserFile[]) => list.forEach((item) => releaseUrl(item.uid));
const goodsUrls = (item: GoodsItem) => item.imageUrls.length ? item.imageUrls : (item.coverImageUrl ? [item.coverImageUrl] : []);
const wantedUrls = (item: WantedItem) => item.imageUrls.length ? item.imageUrls : (item.coverImageUrl ? [item.coverImageUrl] : []);
const beforeImageUpload = (raw: UploadRawFile) => { const ok = raw.type.startsWith('image/'); if (!ok) ElMessage.warning('只能上传图片文件'); return ok; };
const onExceed = () => ElMessage.warning(`最多上传 ${MAX_IMAGE_COUNT} 张图片`);
const previewUpload = (file: UploadFile | UploadUserFile) => { previewUrl.value = String(file.url || ''); previewVisible.value = Boolean(previewUrl.value); };
const onPreview: UploadProps['onPreview'] = (file) => { previewUpload(file); };
const onGoodsUploadChange: UploadProps['onChange'] = (_file, files) => { goodsFiles.value = normList(files as UploadFiles); };
const onWantedUploadChange: UploadProps['onChange'] = (_file, files) => { wantedFiles.value = normList(files as UploadFiles); };
const onGoodsUploadRemove: UploadProps['onRemove'] = (file, files) => { releaseUrl(file.uid); goodsFiles.value = normList(files as UploadFiles); return true; };
const onWantedUploadRemove: UploadProps['onRemove'] = (file, files) => { releaseUrl(file.uid); wantedFiles.value = normList(files as UploadFiles); return true; };
const moveInList = (type: UploadBucket, index: number, offset: number) => {
  const target = type === 'goods' ? goodsFiles : wantedFiles;
  const list = [...target.value];
  const nextIndex = index + offset;
  if (index < 0 || nextIndex < 0 || index >= list.length || nextIndex >= list.length) return;
  const [current] = list.splice(index, 1);
  list.splice(nextIndex, 0, current);
  target.value = list;
};
const setCoverImage = (type: UploadBucket, index: number) => {
  const target = type === 'goods' ? goodsFiles : wantedFiles;
  const list = [...target.value];
  if (index <= 0 || index >= list.length) return;
  const [current] = list.splice(index, 1);
  list.unshift(current);
  target.value = list;
};
const removeVisualImage = (type: UploadBucket, index: number) => {
  const target = type === 'goods' ? goodsFiles : wantedFiles;
  const list = [...target.value];
  const [current] = list.splice(index, 1);
  if (!current) return;
  releaseUrl(current.uid);
  target.value = list;
};
const handleDragStart = (type: UploadBucket, index: number) => { dragState[type] = index; };
const handleDragOver = (event: DragEvent) => { event.preventDefault(); if (event.dataTransfer) event.dataTransfer.dropEffect = 'move'; };
const handleDrop = (type: UploadBucket, dropIndex: number) => {
  const fromIndex = dragState[type];
  const target = type === 'goods' ? goodsFiles : wantedFiles;
  if (fromIndex == null || fromIndex === dropIndex) { dragState[type] = null; return; }
  const list = [...target.value];
  if (fromIndex < 0 || fromIndex >= list.length || dropIndex < 0 || dropIndex >= list.length) { dragState[type] = null; return; }
  const [current] = list.splice(fromIndex, 1);
  list.splice(dropIndex, 0, current);
  target.value = list;
  dragState[type] = null;
};
const clearDragState = (type: UploadBucket) => { dragState[type] = null; };
const closeGoodsDialog = () => { goodsDialog.value = false; goodsEditingId.value = null; Object.assign(goodsForm, makeGoodsForm()); clearList(goodsFiles.value); goodsFiles.value = []; nextTick(() => goodsFormRef.value?.clearValidate()); };
const closeWantedDialog = () => { wantedDialog.value = false; wantedEditingId.value = null; Object.assign(wantedForm, makeWantedForm()); clearList(wantedFiles.value); wantedFiles.value = []; nextTick(() => wantedFormRef.value?.clearValidate()); };
const openGoodsCreate = () => { activeTab.value = 'goods'; closeGoodsDialog(); goodsDialog.value = true; nextTick(() => goodsFormRef.value?.clearValidate()); };
const openWantedCreate = () => { activeTab.value = 'wanted'; closeWantedDialog(); wantedDialog.value = true; nextTick(() => wantedFormRef.value?.clearValidate()); };
const startEditGoods = (item: GoodsItem) => { goodsEditingId.value = item.id; Object.assign(goodsForm, { title: item.title, price: item.price, originalPrice: item.originalPrice || undefined, category: normalizeCategory(item.category), campus: item.campus || defaultCampus.value, condition: item.condition || conditionOptions.value[0] || '全新', intro: item.intro, description: item.description, tagsText: item.tags.join('，'), imageCount: goodsUrls(item).length }); clearList(goodsFiles.value); goodsFiles.value = remoteList(goodsUrls(item)); goodsDialog.value = true; nextTick(() => goodsFormRef.value?.clearValidate()); };
const startEditWanted = (item: WantedItem) => { wantedEditingId.value = item.id; Object.assign(wantedForm, { title: item.title, budget: item.budget, category: normalizeCategory(item.category), campus: item.campus || defaultCampus.value, deadline: item.deadline ? item.deadline.slice(0, 10) : '', intro: item.intro, description: item.description, tagsText: item.tags.join('，') }); clearList(wantedFiles.value); wantedFiles.value = remoteList(wantedUrls(item)); wantedDialog.value = true; nextTick(() => wantedFormRef.value?.clearValidate()); };
const resolveUrls = async (list: UploadUserFile[]) => { const remote = list.filter((item) => !item.raw && typeof item.url === 'string' && item.url && !item.url.startsWith('blob:')).map((item) => String(item.url)); const local = list.filter((item) => item.raw).map((item) => item.raw as UploadRawFile).filter(Boolean) as File[]; if (!local.length) return remote.slice(0, MAX_IMAGE_COUNT); const uploaded = await uploadGoodsImages(local); return [...remote, ...uploaded.map((item) => item.url)].slice(0, MAX_IMAGE_COUNT); };
const submitGoods = async () => { const valid = await goodsFormRef.value?.validate().catch(() => false); if (!valid) return ElMessage.warning('请完善商品信息后再提交'); goodsSubmitting.value = true; try { const payload: CreateGoodsPayload = { title: goodsForm.title.trim(), price: Number(goodsForm.price), originalPrice: goodsForm.originalPrice == null ? null : Number(goodsForm.originalPrice), category: normalizeCategory(goodsForm.category), campus: goodsForm.campus.trim(), condition: goodsForm.condition.trim(), intro: goodsForm.intro.trim(), description: goodsForm.description.trim(), tags: tags(goodsForm.tagsText), imageUrls: await resolveUrls(goodsFiles.value) }; goodsEditingId.value != null ? await updateGoodsPost(goodsEditingId.value, payload) : await createGoodsPost(payload); ElMessage.success(goodsEditingId.value != null ? '商品已更新' : '商品已发布'); closeGoodsDialog(); await loadMyGoods(); } catch (error) { ElMessage.error(err(error, '商品保存失败')); } finally { goodsSubmitting.value = false; } };
const submitWanted = async () => { const valid = await wantedFormRef.value?.validate().catch(() => false); if (!valid) return ElMessage.warning('请完善求购信息后再提交'); wantedSubmitting.value = true; try { const payload: CreateWantedPayload = { title: wantedForm.title.trim(), budget: wantedForm.budget.trim(), category: normalizeCategory(wantedForm.category), campus: wantedForm.campus.trim(), deadline: wantedForm.deadline.trim(), intro: wantedForm.intro.trim(), description: wantedForm.description.trim(), tags: tags(wantedForm.tagsText), imageUrls: await resolveUrls(wantedFiles.value) }; wantedEditingId.value != null ? await updateWantedPost(wantedEditingId.value, payload) : await createWantedPost(payload); ElMessage.success(wantedEditingId.value != null ? '求购已更新' : '求购已发布'); closeWantedDialog(); await loadMyWanted(); } catch (error) { ElMessage.error(err(error, '求购保存失败')); } finally { wantedSubmitting.value = false; } };
const loadCategories = async () => { try { categories.value = (await getCategories()).sort((a, b) => a.sortNum - b.sortNum); } catch { categories.value = []; } };
const loadMyGoods = async () => { goodsLoading.value = true; goodsError.value = ''; try { goodsList.value = await getMyGoodsList(); } catch (error) { goodsError.value = err(error, '请稍后重试'); } finally { goodsLoading.value = false; } };
const loadMyWanted = async () => { wantedLoading.value = true; wantedError.value = ''; try { wantedList.value = await getMyWantedList(); } catch (error) { wantedError.value = err(error, '请稍后重试'); } finally { wantedLoading.value = false; } };
const refreshAll = async () => { refreshing.value = true; await Promise.allSettled([loadCategories(), loadMyGoods(), loadMyWanted()]); refreshing.value = false; };
const goGoodsDetail = (id: number) => router.push({ name: 'goods-detail', params: { id } });
const goWantedDetail = (id: number) => router.push({ name: 'wanted-detail', params: { id } });
const goodsAct = (id: number, type: string) => goodsActionId.value === id && goodsActionType.value === type;
const wantedAct = (id: number, type: string) => wantedActionId.value === id && wantedActionType.value === type;
const offGoods = async (item: GoodsItem) => { goodsActionId.value = item.id; goodsActionType.value = 'off'; try { await offShelfGoodsPost(item.id); ElMessage.success('商品已下架'); await loadMyGoods(); } catch (error) { ElMessage.error(err(error, '商品下架失败')); } finally { goodsActionId.value = null; goodsActionType.value = ''; } };
const relistGoods = async (item: GoodsItem) => { goodsActionId.value = item.id; goodsActionType.value = 'relist'; try { await relistGoodsPost(item.id); ElMessage.success('商品已重新上架'); await loadMyGoods(); } catch (error) { ElMessage.error(err(error, '商品重新上架失败')); } finally { goodsActionId.value = null; goodsActionType.value = ''; } };
const deleteGoods = async (item: GoodsItem) => { try { await ElMessageBox.confirm(`确认删除商品“${item.title}”吗？删除后不可恢复。`, '删除商品', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }); } catch { return; } goodsActionId.value = item.id; goodsActionType.value = 'delete'; try { await deleteGoodsPost(item.id); if (goodsEditingId.value === item.id) closeGoodsDialog(); ElMessage.success('商品已删除'); await loadMyGoods(); } catch (error) { ElMessage.error(err(error, '商品删除失败')); } finally { goodsActionId.value = null; goodsActionType.value = ''; } };
const closeWanted = async (item: WantedItem) => { wantedActionId.value = item.id; wantedActionType.value = 'close'; try { await closeWantedPost(item.id); ElMessage.success('求购已关闭'); await loadMyWanted(); } catch (error) { ElMessage.error(err(error, '求购关闭失败')); } finally { wantedActionId.value = null; wantedActionType.value = ''; } };
const reopenWanted = async (item: WantedItem) => { wantedActionId.value = item.id; wantedActionType.value = 'reopen'; try { await reopenWantedPost(item.id); ElMessage.success('求购已重新开放'); await loadMyWanted(); } catch (error) { ElMessage.error(err(error, '求购重新开放失败')); } finally { wantedActionId.value = null; wantedActionType.value = ''; } };
const deleteWantedItem = async (item: WantedItem) => { try { await ElMessageBox.confirm(`确认删除求购“${item.title}”吗？删除后不可恢复。`, '删除求购', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }); } catch { return; } wantedActionId.value = item.id; wantedActionType.value = 'delete'; try { await deleteWantedPost(item.id); if (wantedEditingId.value === item.id) closeWantedDialog(); ElMessage.success('求购已删除'); await loadMyWanted(); } catch (error) { ElMessage.error(err(error, '求购删除失败')); } finally { wantedActionId.value = null; wantedActionType.value = ''; } };
onMounted(refreshAll); onBeforeUnmount(() => { Array.from(previewMap.values()).forEach((url) => URL.revokeObjectURL(url)); previewMap.clear(); });
</script>

<style scoped lang="scss">
.posts-page { display: grid; gap: 24px; }
.overview-banner { display: flex; justify-content: space-between; gap: 20px; padding: 28px 30px; background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(236, 246, 255, 0.94) 100%); }
.overview-badge { display: inline-flex; align-items: center; min-height: 34px; padding: 0 14px; border-radius: 999px; background: rgba(36, 65, 95, 0.94); color: #fff; font-size: 12px; letter-spacing: .12em; }
.overview-banner h1 { margin: 16px 0 0; color: var(--qh-text-primary); font-size: clamp(28px, 3vw, 40px); }
.overview-banner p { margin: 12px 0 0; color: var(--qh-text-secondary); line-height: 1.9; max-width: 760px; }
.overview-banner__actions { display: flex; flex-direction: column; align-items: flex-end; gap: 12px; }
.stats .stat-card { padding: 22px; }
.stat-card span { color: var(--qh-text-secondary); }
.stat-card strong { display: block; margin-top: 12px; font-size: 30px; color: var(--qh-text-primary); }
.stat-card p { margin: 10px 0 0; color: var(--qh-text-secondary); line-height: 1.7; }
.board { padding: 24px; }
.board__head { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-bottom: 18px; }
.board__head h3 { margin: 0; color: var(--qh-text-primary); }
.board__head p { margin: 8px 0 0; color: var(--qh-text-secondary); line-height: 1.8; }
.board__tags { display: flex; gap: 10px; flex-wrap: wrap; }
.board-summary { margin-bottom: 18px; padding: 16px; display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; }
.board-summary article { padding: 16px; border-radius: 18px; background: rgba(255, 255, 255, 0.78); }
.board-summary span { display: block; color: var(--qh-text-secondary); }
.board-summary strong { display: block; margin-top: 10px; font-size: 22px; color: var(--qh-text-primary); }
.board-summary p { margin: 10px 0 0; color: var(--qh-text-secondary); line-height: 1.7; }
.tabs :deep(.el-tabs__item.is-active) { color: var(--qh-text-primary); font-weight: 700; }
.tabs :deep(.el-tabs__active-bar) { background: var(--qh-primary-deep); }
.toolbar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; padding: 14px; margin-bottom: 14px; }
.toolbar__input { flex: 1 1 320px; }
.toolbar__select { width: 180px; }
.table-shell { padding: 16px; border-radius: 22px; background: rgba(236, 246, 255, 0.46); }
.title-cell strong { color: var(--qh-text-primary); }
.title-cell p { margin: 6px 0 0; color: var(--qh-text-secondary); line-height: 1.6; }
.dialog-form { display: grid; gap: 12px; }
.dialog-form--fancy { gap: 18px; }
.form-grid { display: grid; gap: 14px; }
.form-grid--2 { grid-template-columns: repeat(2, minmax(0, 1fr)); }
.form-grid--3 { grid-template-columns: repeat(3, minmax(0, 1fr)); }
.full-width, :deep(.full-width .el-input-number), :deep(.el-select), :deep(.el-date-editor) { width: 100%; }

:deep(.publish-dialog .el-dialog) {
  overflow: hidden;
  border: 1px solid rgba(148, 189, 226, 0.25);
  border-radius: 28px;
  background:
    radial-gradient(circle at top right, rgba(125, 182, 255, 0.18), transparent 26%),
    linear-gradient(180deg, #fdfefe 0%, #f4f9ff 100%);
  box-shadow: 0 32px 80px rgba(60, 103, 149, 0.18);
}

:deep(.publish-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 0;
}

:deep(.publish-dialog .el-dialog__body) {
  padding: 0 28px 24px;
}

:deep(.publish-dialog .el-dialog__footer) {
  padding: 0 28px 28px;
}

:deep(.publish-dialog .el-dialog__headerbtn) {
  top: 24px;
  right: 24px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 10px 24px rgba(65, 104, 143, 0.12);
}

:deep(.publish-dialog .el-dialog__headerbtn:hover) {
  background: rgba(125, 182, 255, 0.18);
}

.publish-header {
  position: relative;
  padding: 28px 28px 22px;
  background:
    linear-gradient(135deg, rgba(36, 65, 95, 0.98) 0%, rgba(61, 121, 186, 0.96) 52%, rgba(125, 182, 255, 0.9) 100%);
  color: #fff;
}

.publish-header--wanted {
  background:
    linear-gradient(135deg, rgba(32, 39, 51, 0.98) 0%, rgba(73, 114, 173, 0.95) 48%, rgba(129, 188, 255, 0.9) 100%);
}

.publish-header::after {
  content: '';
  position: absolute;
  inset: auto -50px -60px auto;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.24), transparent 68%);
  pointer-events: none;
}

.publish-header__badge {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 14px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
}

.publish-header__content {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  gap: 24px;
  margin-top: 18px;
}

.publish-header__content h2 {
  margin: 0;
  font-size: clamp(26px, 2.8vw, 34px);
  letter-spacing: 0.01em;
}

.publish-header__content p {
  margin: 12px 0 0;
  max-width: 620px;
  color: rgba(255, 255, 255, 0.84);
  line-height: 1.8;
}

.publish-header__meta {
  display: grid;
  gap: 12px;
  min-width: 220px;
  align-content: flex-start;
}

.publish-header__meta span {
  display: inline-flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  min-height: 42px;
  padding: 0 14px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.12);
  color: rgba(255, 255, 255, 0.95);
}

.publish-shell {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 22px;
  padding-top: 22px;
}

.publish-side {
  display: grid;
  gap: 16px;
  align-content: start;
}

.publish-side__card,
.publish-side__metrics article {
  border: 1px solid rgba(145, 187, 224, 0.18);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 16px 34px rgba(116, 160, 207, 0.1);
}

.publish-side__card {
  padding: 18px;
}

.publish-side__card--hero {
  background: linear-gradient(180deg, rgba(236, 246, 255, 0.95) 0%, rgba(255, 255, 255, 0.92) 100%);
}

.publish-side__card--wanted {
  background: linear-gradient(180deg, rgba(244, 248, 255, 0.98) 0%, rgba(255, 255, 255, 0.92) 100%);
}

.publish-side__label {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(61, 121, 186, 0.1);
  color: var(--qh-primary-deep);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.publish-side__card strong {
  display: block;
  margin-top: 14px;
  color: var(--qh-text-primary);
  font-size: 22px;
  line-height: 1.4;
}

.publish-side__card p {
  margin: 10px 0 0;
  color: var(--qh-text-secondary);
  line-height: 1.8;
}

.publish-side__price {
  margin-top: 18px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(61, 121, 186, 0.08);
}

.publish-side__price--wanted {
  background: rgba(73, 114, 173, 0.08);
}

.publish-side__price span {
  color: var(--qh-text-secondary);
  font-size: 13px;
}

.publish-side__price strong {
  margin-top: 8px;
  font-size: 28px;
  color: var(--qh-primary-deep);
}

.publish-progress {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid rgba(148, 189, 226, 0.2);
}

.publish-progress__head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 10px;
}

.publish-progress__head span {
  color: var(--qh-text-secondary);
  font-size: 13px;
}

.publish-progress__head strong {
  margin-top: 0;
  font-size: 16px;
  color: var(--qh-text-primary);
}

.publish-side__metrics {
  display: grid;
  gap: 12px;
}

.publish-side__metrics article {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
}

.publish-side__metrics .el-icon {
  width: 38px;
  height: 38px;
  border-radius: 14px;
  background: rgba(61, 121, 186, 0.1);
  color: var(--qh-primary-deep);
  font-size: 18px;
}

.publish-side__metrics strong {
  display: block;
  color: var(--qh-text-primary);
  font-size: 15px;
}

.publish-side__metrics span {
  display: block;
  margin-top: 4px;
  color: var(--qh-text-secondary);
  font-size: 12px;
}

.publish-checklist {
  margin: 14px 0 0;
  padding-left: 18px;
  color: var(--qh-text-secondary);
}

.publish-checklist li {
  line-height: 1.8;
}

.publish-side__todo {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(148, 189, 226, 0.18);
}

.publish-side__todo strong {
  margin-top: 0;
  font-size: 14px;
}

.publish-chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.publish-chip {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(61, 121, 186, 0.08);
  color: var(--qh-primary-deep);
  font-size: 12px;
  font-weight: 600;
}

.form-section {
  padding: 20px 20px 18px;
  border: 1px solid rgba(146, 187, 223, 0.2);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.65);
}

.form-section__head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 16px;
}

.form-section__eyebrow {
  display: inline-block;
  color: var(--qh-primary-deep);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.form-section__head h3 {
  margin: 8px 0 0;
  color: var(--qh-text-primary);
  font-size: 22px;
}

.form-section__head p {
  margin: 0;
  max-width: 360px;
  color: var(--qh-text-secondary);
  line-height: 1.8;
  text-align: right;
}

.upload-panel {
  padding: 18px;
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(236, 246, 255, 0.68) 0%, rgba(255, 255, 255, 0.92) 100%);
  border: 1px dashed rgba(125, 182, 255, 0.45);
}

.upload-panel--wanted {
  background: linear-gradient(180deg, rgba(241, 246, 255, 0.72) 0%, rgba(255, 255, 255, 0.94) 100%);
}

.upload-panel__tips {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
}

.upload-panel__tips span {
  color: var(--qh-text-primary);
  font-weight: 700;
}

.upload-panel__tips p {
  margin: 0;
  color: var(--qh-text-secondary);
  text-align: right;
  line-height: 1.7;
}

.cover-manager {
  display: grid;
  gap: 14px;
  margin-bottom: 16px;
}

.cover-manager__hero {
  position: relative;
  overflow: hidden;
  min-height: 190px;
  border-radius: 20px;
  background: rgba(36, 65, 95, 0.92);
}

.cover-manager__hero-image {
  display: block;
  width: 100%;
  height: 190px;
  object-fit: cover;
  opacity: 0.78;
}

.cover-manager__hero::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(21, 34, 49, 0.12) 0%, rgba(21, 34, 49, 0.74) 100%);
}

.cover-manager__hero-badge,
.cover-manager__hero-info {
  position: absolute;
  z-index: 1;
}

.cover-manager__hero-badge {
  top: 14px;
  left: 14px;
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  backdrop-filter: blur(10px);
}

.cover-manager__hero-info {
  right: 18px;
  bottom: 18px;
  left: 18px;
}

.cover-manager__hero-info strong {
  display: block;
  color: #fff;
  font-size: 20px;
}

.cover-manager__hero-info span {
  display: block;
  margin-top: 8px;
  color: rgba(255, 255, 255, 0.84);
  line-height: 1.7;
}

.cover-manager__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.cover-card {
  overflow: hidden;
  border: 1px solid rgba(148, 189, 226, 0.22);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: 0 16px 30px rgba(123, 160, 197, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.cover-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 34px rgba(90, 130, 173, 0.14);
}

.cover-card--active {
  border-color: rgba(61, 121, 186, 0.45);
  box-shadow: 0 18px 34px rgba(90, 130, 173, 0.16);
}

.cover-card--dragging {
  opacity: 0.72;
  transform: scale(0.98);
  border-style: dashed;
}

.cover-card__media {
  position: relative;
  height: 124px;
  overflow: hidden;
  background: rgba(230, 238, 247, 0.85);
}

.cover-card__media img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-card__flag {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(61, 121, 186, 0.88);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

.cover-card__drag-hint {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  min-height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(18, 28, 41, 0.48);
  color: rgba(255, 255, 255, 0.92);
  font-size: 12px;
  backdrop-filter: blur(8px);
}

.cover-card__overlay {
  position: absolute;
  inset: auto 10px 10px 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  padding: 8px;
  border-radius: 14px;
  background: rgba(18, 28, 41, 0.54);
  opacity: 0;
  transform: translateY(8px);
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.cover-card:hover .cover-card__overlay {
  opacity: 1;
  transform: translateY(0);
}

.cover-card__meta {
  padding: 12px;
}

.cover-card__meta strong {
  display: block;
  color: var(--qh-text-primary);
  font-size: 14px;
  line-height: 1.4;
}

.cover-card__meta span {
  display: block;
  margin-top: 6px;
  color: var(--qh-text-secondary);
  font-size: 12px;
}

.tag-preview {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-top: -2px;
  margin-bottom: 4px;
}

.tag-preview__label {
  padding-top: 4px;
  color: var(--qh-text-secondary);
  font-size: 13px;
  white-space: nowrap;
}

.tag-preview__list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: var(--qh-primary-deep);
}

.upload-trigger strong {
  font-size: 24px;
  line-height: 1;
}

.upload-trigger small {
  color: var(--qh-text-secondary);
  font-size: 12px;
}

.upload-panel__alert {
  margin-top: 14px;
}

.upload-validator {
  margin-top: 4px;
}

.upload-validator :deep(.el-form-item__content) {
  min-height: 0;
}

.upload-validator__placeholder {
  width: 100%;
  height: 0;
}

.publish-footer {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding: 16px 18px;
  border: 1px solid rgba(148, 189, 226, 0.2);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.82);
}

.publish-footer__hint strong {
  display: block;
  color: var(--qh-text-primary);
}

.publish-footer__hint span {
  display: block;
  margin-top: 6px;
  color: var(--qh-text-secondary);
  line-height: 1.7;
}

.publish-footer__actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

:deep(.publish-dialog .el-form-item) {
  margin-bottom: 0;
}

:deep(.publish-dialog .el-form-item__label) {
  margin-bottom: 8px;
  color: var(--qh-text-primary);
  font-weight: 600;
}

:deep(.publish-dialog .el-form-item.is-error .el-input__wrapper),
:deep(.publish-dialog .el-form-item.is-error .el-textarea__inner),
:deep(.publish-dialog .el-form-item.is-error .el-select__wrapper),
:deep(.publish-dialog .el-form-item.is-error .el-date-editor.el-input__wrapper),
:deep(.publish-dialog .el-form-item.is-error .el-input-number) {
  box-shadow: 0 0 0 1px rgba(245, 108, 108, 0.55) inset, 0 0 0 4px rgba(245, 108, 108, 0.1);
}

:deep(.publish-dialog .el-input__wrapper),
:deep(.publish-dialog .el-textarea__inner),
:deep(.publish-dialog .el-select__wrapper),
:deep(.publish-dialog .el-input-number),
:deep(.publish-dialog .el-date-editor.el-input__wrapper) {
  border-radius: 16px;
  box-shadow: 0 0 0 1px rgba(148, 189, 226, 0.22) inset;
  background: rgba(255, 255, 255, 0.95);
}

:deep(.publish-dialog .el-input__wrapper.is-focus),
:deep(.publish-dialog .el-textarea__inner:focus),
:deep(.publish-dialog .el-select__wrapper.is-focused),
:deep(.publish-dialog .el-date-editor.el-input__wrapper.is-focus),
:deep(.publish-dialog .el-input-number.is-controls-right),
:deep(.publish-dialog .el-input-number:hover) {
  box-shadow: 0 0 0 1px rgba(61, 121, 186, 0.45) inset, 0 0 0 4px rgba(125, 182, 255, 0.12);
}

:deep(.publish-dialog .el-textarea__inner) {
  min-height: 132px;
}

:deep(.publish-dialog .el-upload--picture-card),
:deep(.publish-dialog .el-upload-list--picture-card .el-upload-list__item) {
  border-radius: 18px;
}

.preview-image { display: block; width: 100%; max-height: 72vh; object-fit: contain; }
@media (max-width: 1100px) { .board-summary { grid-template-columns: 1fr; } }
@media (max-width: 960px) {
  .overview-banner, .overview-banner__actions, .board__head, .publish-header__content, .publish-footer, .form-section__head, .upload-panel__tips {
    flex-direction: column;
    align-items: flex-start;
  }
  .publish-shell, .form-grid--2, .form-grid--3 { grid-template-columns: 1fr; }
  .publish-header__meta { width: 100%; min-width: 0; }
  .form-section__head p, .upload-panel__tips p { text-align: left; max-width: none; }
  .cover-manager__grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .tag-preview { flex-direction: column; }
  .publish-footer__actions { width: 100%; justify-content: flex-end; }
}
</style>
