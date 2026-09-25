<template>
  <div class="homeContainer">
    <div class="carousel-margin">
      <div style="flex: 2;background-color: #606266;box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);height: 400px">
        <div v-for="(item,index) in types" :key="index" class="type-item" @click="goPage('/front/goods')">
          <span>{{item.name}}</span>
          <i style="color: #fff;" class="el-icon-arrow-right"></i>
        </div>
      </div>
      <div style="flex: 8">
        <el-carousel height="400px" :interval="10000">
          <el-carousel-item v-for="item in carousels" :key="item.id">
            <img :src="getImageUrl(item.cover)" class="carousel-img" @error="handleImageError" @click="goPage('/front/goods')" style="width: 100%; height: 100%; object-fit: cover; cursor: pointer;">
          </el-carousel-item>
        </el-carousel>
      </div>
    </div>

    <el-card style="margin-top: 20px">
      <div style="font-size: 20px;font-weight: bold;margin-bottom: 15px">公告信息</div>
      <el-collapse v-if="notices.length" v-model="activeNames">
        <el-collapse-item v-for="(item, index) in notices" :key="item.id || index" :title="item.name" :name="index">
          <div>{{ item.content }}</div>
        </el-collapse-item>
      </el-collapse>
      <el-empty v-else description="暂无公告"></el-empty>
    </el-card>

    <div style="margin-top: 30px">
      <div style="display: flex;justify-content: space-between;align-items: center">
        <div style="border-left: 5px solid #ff6700;padding-left: 7px;color:#303133; font-size: 12px;">
          <h1>新品上架</h1>
        </div>
        <div>
          <el-link href="/front/goods" :underline="false">查看更多>></el-link>
        </div>
      </div>
      <div>
        <el-row :gutter="20">
          <el-col :span="6" v-for="(item,index) in timeGoods" :key="index" style="margin-top: 10px">
            <el-card :body-style="{ padding: '0px' }" class="card-item" @click.native="goGoodsDetail(item.id)">
              <img :src="getImageUrl(item.cover)" alt="" @error="handleImageError" style="width: 100%;height: 200px">
              <div style="padding: 10px">
                <div style="margin-top: 3px;font-size: 13px">
                  {{item.name}}
                </div>
                <div style="margin-top: 5px;font-size: 11px;color: #909399;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">
                  {{item.descr}}
                </div>
                <div style="display: flex;justify-content: space-between;align-items: center;margin-top: 10px">
                  <div style="font-size: 20px;color: #FFA500;font-weight: 600">
                    ￥{{item.price}}
                  </div>
                  <div style="font-size:11px;color: #909399;">
                    累计热销：{{item.sales}}
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>

    <div style="margin-top: 30px">
      <div style="display: flex;justify-content: space-between;align-items: center">
        <div style="border-left: 5px solid #ff6700;padding-left: 7px;color:#303133; font-size: 12px;">
          <h1>热销商品</h1>
        </div>
        <div>
          <el-link href="/front/goods" :underline="false">查看更多>></el-link>
        </div>
      </div>
      <div>
        <el-row :gutter="20">
          <el-col :span="6" v-for="(item,index) in salesGoods" :key="index" style="margin-top: 10px">
            <el-card :body-style="{ padding: '0px' }" class="card-item" @click.native="goGoodsDetail(item.id)">
              <img :src="getImageUrl(item.cover)" alt="" @error="handleImageError" style="width: 100%;height: 200px">
              <div style="padding: 10px">
                <div style="margin-top: 3px;font-size: 13px">
                  {{item.name}}
                </div>
                <div style="margin-top: 5px;font-size: 11px;color: #909399;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">
                  {{item.descr}}
                </div>
                <div style="display: flex;justify-content: space-between;align-items: center;margin-top: 10px">
                  <div style="font-size: 20px;color: #FFA500;font-weight: 600">
                    ￥{{item.price}}
                  </div>
                  <div style="font-size:11px;color: #909399;">
                    累计热销：{{item.sales}}
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      carousels: [],
      types: [],
      timeGoods: [],
      salesGoods: [],
      notices: [],
      activeNames: [0]
    }
  },
  created() {
    this.loadType()
    this.loadCarousel()
    this.loadTimeGoods()
    this.loadSaleGoods()
    this.loadNotice()
  },
  methods: {
    loadCarousel(){
      this.$request.get('/carousel/selectAll').then(res => {
        this.carousels = Array.isArray(res.data) ? res.data : []
      }).catch(() => {
        this.carousels = []
      })
    },
    loadType(){
      this.$request.get('/type/selectAll').then(res => {
        this.types = Array.isArray(res.data) ? res.data : []
      })
    },
    loadTimeGoods(){
      this.$request.get('/goods/times').then(res => {
        this.timeGoods = Array.isArray(res.data) ? res.data : []
      })
    },
    loadSaleGoods(){
      this.$request.get('/goods/sales').then(res => {
        this.salesGoods = Array.isArray(res.data) ? res.data : []
      })
    },
    goPage(url){
      location.href = url
    },
    goGoodsDetail(id){
      if (!id) {
        this.$message.error('商品信息不存在')
        return
      }
      this.$router.push({ name: 'GoodsDetail', query: { id } })
    },
    loadNotice() {
      this.$request.get('/notice/selectAll').then(res => {
        this.notices = Array.isArray(res.data) ? res.data : []
      }).catch(() => {
        this.notices = []
      })
    },
    getImageUrl(url) {
      if (!url) return require('@/assets/empty.svg')
      if (/^data:/i.test(url)) return url
      if (/^https?:\/\//i.test(url)) {
        try {
          const imageUrl = new URL(url)
          if (imageUrl.pathname.indexOf('/file/download/') === 0) {
            return this.$baseUrl + imageUrl.pathname + imageUrl.search
          }
        } catch (e) {
          return require('@/assets/empty.svg')
        }
        return url
      }
      return this.$baseUrl + (url.startsWith('/') ? '' : '/') + url
    },
    handleImageError(event) {
      const fallback = require('@/assets/empty.svg')
      if (event.target.src !== fallback) event.target.src = fallback
    }
  }
}
</script>

<style scoped>
.homeContainer{
  width: 70%;
  margin: 0 auto;
  min-height: 90vh;
}

.carousel-margin{
  margin: 10px 0;
  display: flex;
}

.carousel-img{
  width: 100%;
}

.type-item{
  padding: 0 30px;
  margin: 10px 0;
  height: 33px;
  line-height: 33px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #fff;
  cursor: pointer;
}

.type-item:hover{
  background-color: #ff6700;
}

.card-item:hover{
  cursor: pointer;
  transform: scale(1.03);
}
/* 1. 确保包裹两个组件的 Flex 容器占满宽度，并且没有内边距导致它们无法靠边 */
.menu-and-carousel-container {
  display: flex;
  justify-content: space-between; /* 或者使用其他布局，确保右侧靠边 */
  padding: 0; /* 核心修改：去掉任何可能导致无法靠边的内边距 */
  margin-top: 0; /* 核心修改：去掉顶部 margin，让它和导航栏对齐 */
  width: 100%; /* 确保容器撑满屏幕宽度，除非布局有特殊要求 */
}

/* 2. 确保左侧菜单高度占满它所在的侧边栏空间 */
.left-menu {
  flex: 0 0 calc(20%); /* 或者固定宽度 */
  height: 100%; /* 如果希望左侧占满父容器高度 */
  display: flex;
  flex-direction: column;
}

/* 3. 确保轮播图组件所在的容器也占满剩余空间并右对齐 */
.carousel-wrapper {
  flex: 1; /* 核心修改：占据剩余所有空间 */
  display: flex;
  justify-content: flex-end; /* 确保右对齐 */
  overflow: hidden; /* 防止内部内容撑破布局 */
}
</style>