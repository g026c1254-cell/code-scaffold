<template>
  <div class="goods-page">
    <!-- 顶部工具栏+搜索框 -->
    <div class="goods-toolbar">
      <div>
        <h1 class="page-title">人気商品</h1>
      </div>
      <div class="search-wrap">
        <input v-model="keyboard" type="text" placeholder="商品名を検索" class="search-input" @keyup.enter="loadGoods"/>
        <el-button class="search-button" @click="loadGoods">
          <i class="el-icon-search search-icon"></i>
        </el-button>
      </div>
    </div>

    <!-- 分类按钮：完全透明、清爽轮廓风格 -->
    <div class="category-area">
      <div class="type-group">
        <button
          type="button"
          class="type-chip-btn"
          :class="{ 'type-selected': selectedCategoryId === 0 }"          @click="handleAllClick">
          すべて
        </button>
        <button
          type="button"
          class="type-chip-btn"
          v-for="(category,index) in types"
          :key="index"
          :class="{ 'type-selected': selectedCategoryId === category.id }"          @click="handleCategoryClick(category)">
          {{ displayTypeName(category.name) }}
        </button>
      </div>
    </div>

    <!-- 商品网格列表：向前紧凑对齐，无散落空白 -->
    <div>
      <div v-if="goods.length > 0" class="goods-grid-wrapper">
        <div
          v-for="(item, index) in goods"
          :key="item.id || index"
          class="card-item"
          @click="goDetail(item.id)"
        >
          <!-- 商品封面图片容器：等比缩小显示完整商品，绝不裁剪 -->
          <div class="goods-image-box">
            <img :src="getImageUrl(item.cover)" @error="handleImageError" :alt="item.name" class="goods-image">
          </div>
          <div class="goods-content">
            <div class="goods-name" :title="item.name">
              {{ item.name }}
            </div>
            <div class="goods-descr" :title="stripHtml(item.content || item.descr)">
              {{ stripHtml(item.content || item.descr) || '' }}
            </div>
            <div class="goods-footer-row">
              <div class="goods-price">{{ item.price }}円</div>
              <div class="publisher-tag" :title="item.userName || $t('common.anonymous')">
                <i class="el-icon-user"></i> {{ item.userName || $t('common.anonymous') }}
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="total > 0" style="margin-top: 24px; text-align: right;">
        <el-pagination
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[20, 40, 60]"
            :page-size="pageSize"
            layout="total, prev, pager, next, jumper"
            :total="total"
            background
        ></el-pagination>
      </div>
    </div>

    <div v-if="goods.length == 0">
      <el-empty :image-size="300" :image="require('@/assets/empty.svg')" description="商品がありません"></el-empty>
    </div>
  </div>
</template>

<script>
export default {
  name: "Goods",
  data() {
    return {
      types: [],
      selectedCategoryId: parseInt(this.$route.query.selectedCategoryId) || 0,
      total: 0,
      pageNum: 1,
      pageSize: 20,
      keyboard: this.$route.query.name || '',
      goods: [],
    }
  },
  created() {
    this.loadType()
    this.loadGoods()
  },
  watch: {
    '$route.query.name'(value) {
      this.keyboard = value || ''
      this.pageNum = 1
      this.loadGoods()
    },
    '$route.query.selectedCategoryId'(value) {
      const id = parseInt(value) || 0
      if (this.selectedCategoryId !== id) {
        this.selectedCategoryId = id
        this.pageNum = 1
        this.loadGoods()
      }
    }
  },
  methods: {
    getImageUrl(url) {
      if (!url) return require('@/assets/empty.svg')
      if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
        return url
      }
      return this.$baseUrl + (url.startsWith('/') ? url : '/' + url)
    },
    handleImageError(e) {
      e.target.src = require('@/assets/empty.svg')
    },
    displayTypeName(name) {
      if (!name) return ''
      const categoryMap = {
        '零食': 'お菓子・食品',
        '饮料': '飲料・ドリンク',
        '数码产品': '家電・スマホ',
        '女装': 'レディース',
        '男装': 'メンズ',
        '家具': 'インテリア・家具',
        '办公用品': '文房具・日用品',
        '图书': '本・教科書',
        '美妆': 'コスメ・美容',
        '食品': 'お菓子・食品',
        '日用品': '文房具・日用品'
      }
      if (categoryMap[name]) return categoryMap[name]
      const key = 'category.' + name
      const translated = this.$t(key)
      return translated === key ? name : translated
    },
    goDetail(id) {
      if (!id) {
        this.$message.error('商品情報が見つかりません')
        return
      }
      this.$router.push({ name: 'GoodsDetail', query: { id } })
    },
    loadType() {
      this.$request.get('/type/selectAll').then(res => {
        this.types = Array.isArray(res.data) ? res.data : []
      })
    },
    loadGoods() {
      this.$request.get("/goods/selectPage/type", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.keyboard,
          typeId: this.selectedCategoryId
        }
      }).then(res => {
        this.goods = res.data?.records || []
        this.total = res.data?.total || 0
      })
    },
    handleAllClick() {
      if (this.selectedCategoryId === 0) return
      this.selectedCategoryId = 0
      this.pageNum = 1
      this.$router.replace({
        query: { ...this.$route.query, selectedCategoryId: 0 }
      }).catch(() => {})
      this.loadGoods()
    },
    handleCategoryClick(category) {
      if (this.selectedCategoryId === category.id) return
      this.selectedCategoryId = category.id
      this.pageNum = 1
      this.$router.replace({
        query: { ...this.$route.query, selectedCategoryId: category.id }
      }).catch(() => {})
      this.loadGoods()
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.loadGoods()
    },
    stripHtml(value) {
      const container = document.createElement('div')
      container.innerHTML = value || ''
      return (container.textContent || container.innerText || '').replace(/\s+/g, ' ').trim()
    }
  }
}
</script>

<style scoped>
.goods-page {
  width: min(1240px, 94%);
  min-height: 90vh;
  margin: 20px auto;
}

.goods-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.search-wrap {
  display: flex;
  align-items: center;
}

.search-input {
  width: 240px;
  padding: 12px 16px;
  outline: none;
  border: 1px solid #fed7aa;
  border-radius: 20px 0 0 20px;
  font-size: 13px;
  box-sizing: border-box;
  background: #fff;
  transition: border-color .2s ease;
}

.search-input:focus {
  border-color: #ff8a3d;
}

.search-button {
  padding: 12px 18px;
  background: linear-gradient(135deg, #ffa86b 0%, #ff7e29 100%);
  border: none;
  border-radius: 0 20px 20px 0;
  color: #fff;
  cursor: pointer;
}

.search-button:hover {
  opacity: .92;
}

.search-icon {
  font-size: 14px;
}

.page-title {
  border-left: 5px solid #ff8a3d;
  padding-left: 10px;
  color: #1e293b;
  font-size: 22px;
}

.category-area {
  margin-top: 20px;
}

.type-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}

/* 分类小模块去掉背景色：完全透明、清爽轮廓风格 */
.type-chip-btn {
  background: transparent !important;
  background-color: transparent !important;
  border: 1px solid #fed7aa;
  color: #64748b;
  border-radius: 18px;
  padding: 8px 18px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  outline: none;
  box-shadow: none !important;
  transition: all .2s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.type-chip-btn:hover {
  background: transparent !important;
  background-color: transparent !important;
  border-color: #ff8a3d;
  color: #ff8a3d;
  transform: translateY(-1px);
}

.type-chip-btn.type-selected {
  background: transparent !important;
  background-color: transparent !important;
  border: 2px solid #ff8a3d !important;
  color: #ea6b1f !important;
  font-weight: 700 !important;
  box-shadow: none !important;
}

/* 商品卡片网格布局：严格向前对齐，列宽一致，消除散落空白 */
.goods-grid-wrapper {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 18px;
  width: 100%;
}

@media (max-width: 992px) {
  .goods-grid-wrapper {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }
}

@media (max-width: 640px) {
  .goods-grid-wrapper {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
}

.card-item {
  height: 310px;
  min-height: 310px;
  max-height: 310px;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, .8);
  box-shadow: 0 3px 10px rgba(15, 23, 42, .04);
  transition: transform .25s ease, box-shadow .25s ease, border-color .25s ease;
  overflow: hidden;
  cursor: pointer;
  box-sizing: border-box;
}

.card-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 22px rgba(255, 126, 41, .15);
  border-color: #ffd8be;
}

/* 商品封面图片容器：等比缩小居中显示完整商品，绝不裁剪 */
.goods-image-box {
  width: 100%;
  height: 195px;
  min-height: 195px;
  max-height: 195px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  overflow: hidden;
  border-bottom: 1px solid #f1f5f9;
  padding: 8px;
  box-sizing: border-box;
}

.goods-image {
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
  object-fit: contain !important;
  display: block;
  transition: transform .3s ease;
}

.card-item:hover .goods-image {
  transform: scale(1.05);
}

.goods-content {
  height: 115px;
  min-height: 115px;
  max-height: 115px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-sizing: border-box;
}

.goods-name {
  color: #1e293b;
  font-size: 14px;
  font-weight: 600;
  line-height: 19px;
  height: 38px;
  min-height: 38px;
  max-height: 38px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  text-overflow: ellipsis;
  word-break: break-all;
}

.goods-descr {
  margin-top: 2px;
  color: #94a3b8;
  font-size: 12px;
  line-height: 16px;
  height: 16px;
  min-height: 16px;
  max-height: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.goods-footer-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 28px;
  min-height: 28px;
  max-height: 28px;
  padding-top: 6px;
  border-top: 1px dashed #f1f5f9;
  box-sizing: border-box;
}

.goods-price {
  color: #ff7e29;
  font-size: 17px;
  font-weight: 700;
  line-height: 1;
}

.publisher-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  max-width: 110px;
  padding: 3px 8px;
  border-radius: 6px;
  color: #64748b;
  background: #f8fafc;
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.2;
}

@media (max-width: 768px) {
  .goods-toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  .search-wrap {
    width: 100%;
  }
  .search-input {
    flex: 1;
    width: 100%;
  }
}

@media (max-width: 640px) {
  .card-item {
    height: 260px;
    min-height: 260px;
    max-height: 260px;
  }
  .goods-image-box {
    height: 150px;
    min-height: 150px;
    max-height: 150px;
    padding: 6px;
  }
  .goods-content {
    height: 110px;
    min-height: 110px;
    max-height: 110px;
    padding: 8px 10px;
  }
}
</style>
