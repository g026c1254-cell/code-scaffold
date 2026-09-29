<template>
  <div class="manager-goods-page">
    <!-- 顶部+搜索框 -->
    <div class="goods-header-bar">
      <div>
        <h1 class="page-title">人気商品</h1>
      </div>
      <div class="search-box-wrap">
        <input v-model='keyboard' type="text" placeholder="商品名を検索" class="search-input" @keyup.enter="loadGoods"/>
        <el-button class="search-button" @click="loadGoods">
          <i class="el-icon-search"></i>
        </el-button>
      </div>
    </div>

    <!-- 分类按钮：完全透明无背景色风格 -->
    <div class="category-filter-area">
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
      <el-row :gutter="16" v-if="goods.length > 0">
        <el-col :xs="12" :sm="8" :md="6" v-for="(item,index) in goods" :key="index" style="margin-top: 16px">
          <el-card :body-style="{ padding: '0px' }" class="card-item">
            <div class="goods-image-box">
              <img :src="item.cover" alt="" class="goods-image">
            </div>
            <div class="goods-info-body">
              <div class="goods-name-text">
                {{item.name}}
              </div>
              <div class="goods-descr-text">
                {{ stripHtml(item.content || item.descr) }}
              </div>
              <div class="goods-price-row">
                <div class="goods-price-value">
                  {{item.price}}円
                </div>
              </div>
              <div class="goods-publisher-tag">
                投稿者：{{ item.userName || '匿名ユーザー' }}
              </div>
              <el-button
                  type="danger"
                  plain
                  size="mini"
                  class="delete-button"
                  @click.stop="deleteGoods(item.id)">
                商品を削除
              </el-button>
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
      <el-empty :image-size="200" :image="require('@/assets/empty.svg')" description="商品がありません"></el-empty>
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
      keyboard: '',
      goods: [],
    }
  },
  created() {
    this.loadType()
    this.loadGoods()
  },
  methods:{
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
    deleteGoods(id) {
      this.$confirm('この商品を削除してもよろしいですか？', '削除確認', {
        type: 'warning'
      }).then(() => {
        this.$request.delete('/goods/delete?id=' + id).then(res => {
          if (res.code === '200') {
            this.$notify.success({
              title: '完了',
              message: '商品を削除しました',
              showClose: false,
              duration: 2000
            })
            this.loadGoods()
          } else {
            this.$notify.error({
              title: 'エラー',
              message: res.msg,
              showClose: false,
              duration: 2000
            })
          }
        })
      }).catch(() => {})
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
.manager-goods-page {
  padding: 8px 4px;
  min-height: 85vh;
}

.goods-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.page-title {
  border-left: 5px solid #ff8a3d;
  padding-left: 10px;
  font-size: 20px;
  color: #1e293b;
  margin: 0;
}

.search-box-wrap {
  display: flex;
  align-items: center;
}

.search-input {
  width: 220px;
  padding: 10px 14px;
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
  padding: 10px 16px;
  background: linear-gradient(135deg, #ffa86b 0%, #ff7e29 100%);
  border: none;
  border-radius: 0 20px 20px 0;
  color: #fff;
  cursor: pointer;
}

.search-button:hover {
  opacity: .92;
}

.category-filter-area {
  margin-top: 16px;
}

.type-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

/* 分类小模块去掉背景色 */
.type-chip-btn {
  background: transparent !important;
  background-color: transparent !important;
  border: 1px solid #fed7aa;
  color: #64748b;
  border-radius: 18px;
  padding: 6px 14px;
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
  border-color: #ff8a3d;
  color: #ff8a3d;
  transform: translateY(-1px);
}

.type-chip-btn.type-selected {
  background: transparent !important;
  border: 2px solid #ff8a3d !important;
  color: #ea6b1f !important;
  font-weight: 700 !important;
}

.card-item {
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, .8);
  box-shadow: 0 4px 16px rgba(15, 23, 42, .03);
  transition: transform .25s ease, box-shadow .25s ease;
  overflow: hidden;
  background: #ffffff;
}

.card-item:hover {
  cursor: pointer;
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(255, 126, 41, .15);
}

/* 等比缩小展示完整商品图片，绝不裁切 */
.goods-image-box {
  width: 100%;
  height: 180px;
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
  object-fit: contain !important;
  display: block;
}

.goods-info-body {
  padding: 12px;
}

.goods-name-text {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.goods-descr-text {
  margin-top: 5px;
  font-size: 12px;
  color: #94a3b8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.goods-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.goods-price-value {
  font-size: 18px;
  color: #ff7e29;
  font-weight: 700;
}

.goods-publisher-tag {
  margin-top: 6px;
  color: #64748b;
  font-size: 11px;
}

.delete-button {
  margin-top: 10px;
  width: 100%;
  border-radius: 8px;
}
</style>
