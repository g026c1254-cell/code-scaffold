<template>
  <div class="collect-page">
    <!-- 顶部标题 -->
    <div class="collect-toolbar">
      <h1 class="page-title">{{ $t('common.collection') }}</h1>
    </div>

    <div>
      <el-row :gutter="20" v-if="collects.length > 0">
        <el-col :xs="12" :sm="8" :md="6" v-for="(item, index) in collects" :key="index" class="collect-col">
          <el-card :body-style="{ padding: '0px' }" class="card-item" @click.native="goDetail(item.goodsId)">
            <img :src="getImageUrl(item.goods && item.goods.cover)" alt="" class="goods-image" @error="handleImageError">
            <div class="goods-content">
              <div class="goods-name">
                {{ item.goods ? item.goods.name : '' }}
              </div>
              <div class="goods-descr">
                {{ item.goods ? item.goods.descr : '' }}
              </div>
              <div class="goods-meta">
                <div class="goods-price">
                  {{ item.goods ? item.goods.price : 0 }}円
                </div>
                <div>
                  <el-button type="text" class="remove-btn" @click.stop="del(item)">{{ $t('common.cancelCollection') }}</el-button>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div v-if="collects.length === 0" class="empty-state">
      <el-empty :image-size="200" :image="require('@/assets/empty.svg')" :description="$t('common.noCollection')"></el-empty>
    </div>
  </div>
</template>

<script>
export default {
  name: "Collect",
  data() {
    return {
      collects: [],
    }
  },
  created() {
    this.loadCollect()
  },
  methods: {
    loadCollect() {
      this.$request.get('/collect/myCollect').then(res => {
        this.collects = Array.isArray(res.data) ? res.data : []
      }).catch(() => {
        this.collects = []
      })
    },
    goDetail(goodsId) {
      if (!goodsId) return
      this.$router.push({ path: '/front/goodsDetail', query: { id: goodsId } })
    },
    del(row) {
      this.$confirm(this.$t('common.cancelCollection') + '?', {
        type: 'warning'
      }).then(() => {
        this.$request.delete('/collect/delete?id=' + row.id).then(res => {
          if (res.code === '200') {
            this.$message.success(this.$t('common.cancelCollection'))
            this.loadCollect()
          } else {
            this.$message.error(res.msg || this.$t('common.cancelCollection'))
          }
        })
      }).catch(() => {})
    },
    getImageUrl(url) {
      if (!url) return require('@/assets/empty.svg')
      if (/^data:/i.test(url) || /^https?:/i.test(url)) return url
      return this.$baseUrl + (url.startsWith('/') ? '' : '/') + url
    },
    handleImageError(event) {
      event.target.src = require('@/assets/empty.svg')
    }
  }
}
</script>

<style scoped>
.collect-page {
  width: min(1180px, 94%);
  min-height: 90vh;
  margin: 20px auto 40px;
}

.collect-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  border-left: 5px solid #ff8a3d;
  padding-left: 10px;
  color: #303133;
  font-size: 22px;
  margin: 0;
}

.collect-col {
  margin-top: 18px;
}

.card-item {
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, .8);
  box-shadow: 0 10px 25px -5px rgba(15, 23, 42, .08);
  transition: transform .25s ease, box-shadow .25s ease;
  overflow: hidden;
  cursor: pointer;
}

.card-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 24px rgba(48, 49, 51, .12);
}

.goods-image {
  display: block;
  width: 100%;
  height: 200px;
  object-fit: cover;
  background: #f8fafc;
}

.goods-content {
  padding: 12px;
}

.goods-name {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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
  color: #ff7e29;
  font-size: 18px;
  font-weight: 700;
}

.remove-btn {
  color: #ff8a3d;
  padding: 0;
  font-size: 12px;
}

.empty-state {
  margin-top: 40px;
}

@media (max-width: 700px) {
  .collect-page {
    width: auto;
    min-height: calc(100vh - 180px);
    margin: 14px 12px 28px;
  }

  .collect-col {
    margin-top: 14px;
  }

  .goods-image {
    height: 160px;
    object-fit: contain;
  }

  .page-title {
    font-size: 19px;
  }

  .goods-price {
    font-size: 16px;
  }
}

@media (max-width: 520px) {
  .goods-image {
    height: 140px;
  }

  .goods-content {
    padding: 9px;
  }
}
</style>
