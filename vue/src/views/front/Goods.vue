<template>
  <div class="goods-page">
    <!--顶部+搜索框-->
    <div class="goods-toolbar">
      <div>
        <h1 class="page-title">热卖商品</h1>
      </div>
      <div>
        <input v-model='keyboard' type="text" placeholder="请输入搜索商品名称" class="search-input" @keyup.enter="loadGoods"/>
        <el-button class="search-button" @click="loadGoods">
          <i class="search-icon">🔍</i>
        </el-button>
      </div>
    </div>

    <!--分类按钮-->
    <div class="category-area">
      <div class="type-group">
        <el-button type="primary" :class="{ 'type-selected': selectedCategoryId === 0 }" @click="handleAllClick">全部</el-button>
        <el-button type="primary" v-for="(category,index) in types" :key="index" :class="{ 'type-selected': selectedCategoryId === category.id }" @click="handleCategoryClick(category)">
          {{ category.name }}
        </el-button>
      </div>
    </div>
    <div>
      <el-row :gutter="20" v-if="goods.length > 0">
        <el-col :xs="12" :sm="8" :md="6" v-for="(item,index) in goods" :key="index" class="goods-col">
          <el-card :body-style="{ padding: '0px' }" class="card-item" @click.native="goDetail(item.id)">
            <img :src="item.cover" alt="" class="goods-image">
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
      <el-empty :image-size="300" :image="require('@/assets/empty.svg')" description="没有商品哟~"></el-empty>    </div>
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
    goDetail(id) {
      if (!id) {
        this.$message.error('商品信息不存在')
        return
      }
      this.$router.push({ name: 'GoodsDetail', query: { id } })
    },
    loadType(){
      this.$request.get('/type/selectAll').then(res => {
        this.types = res.data
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
        this.goods = res.data?.records
        this.total = res.data?.total
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
.search-input{
  width: 240px;
  padding: 12px 16px;
  outline: none;
  border: 1px solid #dcdfe6;
  border-radius: 20px 0 0 20px;
  font-size: 13px;
  box-sizing: border-box;
}

.search-button{
  padding: 12px 18px;
  background: #ff6700;
  border: none;
  border-radius: 0 20px 20px 0;
}

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

.page-title {
  border-left: 5px solid #ff6700;
  padding-left: 10px;
  color: #303133;
  font-size: 22px;
}

.category-area {
  margin-top: 20px;
}

.type-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

/* 选中状态样式 */
.type-selected {
  background-color: #ff6700 !important;
  color: #fff !important;
}

/* 未选中状态 hover效果 */
.type-group .el-button--primary:not(.type-selected):hover {
  background-color: #ff6700 !important;
  color: #fff !important;
}

/* 重置 ElementUI 主按钮默认样式 */
.type-group .el-button--primary {
  background-color: #fff;
  border-color: #dcdfe6;
  color: #606266;
}

.card-item {
  border-radius: 8px;
  transition: transform .25s ease, box-shadow .25s ease;
}

.card-item:hover{
  cursor: pointer;
  transform: translateY(-5px);
  box-shadow: 0 10px 24px rgba(48, 49, 51, .12);
}

.goods-col {
  margin-top: 18px;
}

.goods-image {
  display: block;
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.goods-content {
  padding: 12px;
}

.goods-name {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.goods-descr {
  margin-top: 7px;
  color: #909399;
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
  color: #ff6700;
  font-size: 20px;
  font-weight: 700;
}

.publisher-tag {
  display: inline-block;
  margin-top: 8px;
  padding: 3px 8px;
  border-radius: 12px;
  color: #606266;
  background: #f4f4f5;
  font-size: 12px;
}

@media (max-width: 700px) {
  .goods-toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .search-input {
    width: calc(100% - 52px);
  }
}
</style>