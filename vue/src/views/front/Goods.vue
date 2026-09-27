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

    <!-- 分类按钮：已按要求彻底去除所有背景色（透明背景） -->
    <div class="category-area">
      <div class="type-group">
        <button
          type="button"
          class="type-chip-btn"
          :class="{ 'type-selected': selectedCategoryId === 0 }"
          @click="handleAllClick">
          すべて
        </button>
        <button
          type="button"
          class="type-chip-btn"
          v-for="(category,index) in types"
          :key="index"
          :class="{ 'type-selected': selectedCategoryId === category.id }"
          @click="handleCategoryClick(category)">
          {{ displayTypeName(category.name) }}
        </button>
      </div>
    </div>

    <div>
      <el-row :gutter="20" v-if="goods.length > 0">
        <el-col :xs="12" :sm="8" :md="6" v-for="(item,index) in goods" :key="index" class="goods-col">
          <el-card :body-style="{ padding: '0px' }" class="card-item" @click.native="goDetail(item.id)">
            <!-- 商品封面图片容器：等比缩小显示完整商品，绝不裁切 -->
            <div class="goods-image-box">
              <img :src="item.cover" :alt="item.name" class="goods-image">
            </div>
            <div class="goods-content">
              <div class="goods-name">
                {{item.name}}
              </div>
              <div class="goods-descr">
                {{ stripHtml(item.content || item.descr) }}
              </div>
              <div class="goods-meta">
                <div class="goods-price">{{item.price}}円</div>
              </div>
              <div class="publisher-tag">{{ $t('common.publisher') }}：{{ item.userName || $t('common.anonymous') }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <div v-if="total > 0" style="margin-top: 20px; text-align: right;">
        <el-pagination
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[8, 16, 32]"
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
  data(){
    return{
      types: [],
      selectedCategoryId: parseInt(this.$route.query.selectedCategoryId) || 0,
      total: 0,
      pageNum: 1,
      pageSize: 8,
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
    }
  },
  methods:{
    displayTypeName(name) {
      if (!name) return ''
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
    loadType(){
      this.$request.get('/type/selectAll').then(res => {
        this.types = Array.isArray(res.data) ? res.data : []
      })
    },
    loadGoods(){
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
      this.selectedCategoryId = 0;
      this.$router.replace({
        query: { ...this.$route.query, selectedCategoryId: 0 }
      })
      this.loadGoods()
    },
    handleCategoryClick(category) {
      this.selectedCategoryId = category.id;
      this.$router.replace({
        query: { ...this.$route.query, selectedCategoryId: category.id }
      })
      this.loadGoods()
    },
    handleCurrentChange(pageNum){
      this.pageNum = pageNum;
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
  width: min(1180px, 92%);
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

.search-input{
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

.search-button{
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

/* 未选中状态 hover效果：依然无背景色，淡橘色边框与文字 */
.type-chip-btn:hover {
  background: transparent !important;
  background-color: transparent !important;
  border-color: #ff8a3d;
  color: #ff8a3d;
  transform: translateY(-1px);
}

/* 选中状态样式：去掉背景色，2px淡橘强调边框与深橘文字 */
.type-chip-btn.type-selected {
  background: transparent !important;
  background-color: transparent !important;
  border: 2px solid #ff8a3d !important;
  color: #ea6b1f !important;
  font-weight: 700 !important;
  box-shadow: none !important;
}

.card-item {
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, .8);
  box-shadow: 0 4px 16px rgba(15, 23, 42, .04);
  transition: transform .25s ease, box-shadow .25s ease;
  overflow: hidden;
  background: #ffffff;
}

.card-item:hover{
  cursor: pointer;
  transform: translateY(-4px);
  box-shadow: 0 10px 24px rgba(255, 126, 41, .15);
}

.goods-col {
  margin-top: 18px;
}

/* 商品封面图片容器：等比缩放展示，居中纯白衬托，不截断 */
.goods-image-box {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  overflow: hidden;
  border-bottom: 1px solid #f8fafc;
}

.goods-image {
  max-width: 100%;
  max-height: 100%;
  width: 100%;
  height: 100%;
  object-fit: contain !important; /* 等比缩小完整显示图片，绝不裁切部分 */
  display: block;
}

.goods-content {
  padding: 12px;
}

.goods-name {
  color: #1e293b;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.goods-descr {
  margin-top: 7px;
  color: #94a3b8;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.goods-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.goods-price {
  color: #ff7e29;
  font-size: 20px;
  font-weight: 700;
}

.publisher-tag {
  display: inline-block;
  max-width: 100%;
  margin-top: 8px;
  padding: 3px 8px;
  border-radius: 12px;
  color: #64748b;
  background: #f1f5f9;
  font-size: 11px;
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
  /* 手机端：商品封面等比缩小显示，绝不被裁切 */
  .goods-image-box {
    height: 150px;
    background: #ffffff;
  }
  .goods-image {
    max-width: 100%;
    max-height: 100%;
    width: 100%;
    height: 100%;
    object-fit: contain !important;
  }
}
</style>
