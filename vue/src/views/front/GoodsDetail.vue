<template>
  <div class="goods-detail-page">
    <div style="display: flex">
      <div style="flex: 4">
        <el-image v-if="goods.cover" style="width: 400px; height: 300px" :src="goods.cover" fit="cover" :preview-src-list="[goods.cover]"></el-image>
      </div>
      <div style="flex: 6">
        <div style="padding: 0 15px">
          <b style="font-size: 20px;color: #303133;">{{goods.name}}</b>
        </div>
        <div style="padding: 0 15px;margin-top: 10px;">
          <div class="goods-summary" v-html="summaryHtml"></div>
          <div style="border-bottom: 1px dashed #eaeaea;margin-top: 10px"></div>
        </div>
        <div style="display: flex;padding: 0 15px;margin-top: 20px;align-items: center">
          <div>
            <span style="font-size: 11px;color: #606266">{{ $t('common.price') }}：</span>
            <span style="font-size: 17px;color: #ff6700;font-weight: bold">{{goods.price}}円</span>
          </div>
          <div style="margin-left: 20px">
            <span style="font-size: 11px;color: #606266">{{ $t('common.stock') }}：{{goods.store}}</span>
          </div>
        </div>
        <div class="publisher-detail">{{ $t('common.publisher') }}：{{ goods.userName || $t('common.anonymous') }}</div>
        <div style="padding: 0 15px;margin-top: 20px">
          <span style="font-size: 11px;color: #606266">{{ $t('common.listedAt') }}：{{goods.date}}</span>
        </div>
        <div style="display: flex;padding: 0 15px;margin-top: 15px;gap: 20px">
          <div>
            <el-input-number v-model="num" @change="handleChange" :min="1" :max="10" :label="$t('common.stock')"></el-input-number>
          </div>
          <div>
            <el-button type="primary" style="width: 180px" @click="buy">{{ $t('common.buyNow') }}</el-button>
          </div>
          <div>
            <el-button :type="isCollect ? 'danger' : 'warning'" style="width: 180px" @click="collect">{{isCollect ? $t('common.favorited') : $t('common.favorite')}}</el-button>
          </div>
        </div>
      </div>
    </div>

    <div style="margin-top: 30px">
      <el-card>
        <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
          <el-tab-pane :label="$t('common.detailIntroduction')" name="goods">
            <div class="w-e-text goods-content-detail" v-html="goods.content || summaryHtml"></div>
          </el-tab-pane>
          <el-tab-pane :label="$t('common.purchaseNotice')" name="notice">
            <div style="padding: 25px;">
              <h3 style="color: #333;margin: 15px 0;">{{ $t('common.purchaseGuide') }}</h3>
              <div>
                <div style="margin: 10px 0;color: #666;">1、{{ $t('common.genuine') }}</div>
                <div style="margin: 10px 0;color: #666;">2、{{ $t('common.returnPolicy') }}</div>
                <div style="margin: 10px 0;color: #666;">3、{{ $t('common.freeShipping') }}</div>
                <div style="margin: 10px 0;color: #666;">4、{{ $t('common.afterSales') }}</div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  name: "GoodsDetail",
  data(){
    return{
      id: this.$route.query.id,
      goods: {},
      num: 1,
      activeName: 'goods',
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      isCollect: false
    }
  },
  created() {
    this.loadGoods()
  },
  methods:{
    escapeHtml(value) {
      return String(value || '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;')
    },
    stripHtml(value) {
      const container = document.createElement('div')
      container.innerHTML = value || ''
      return (container.textContent || container.innerText || '').replace(/\s+/g, ' ').trim()
    },
    loadGoods(){
      this.$request.get('/goods/selectById?id=' + this.id).then(res => {
        this.goods = res.data
        this.isCollect = this.goods.isCollect
      })
    },
    handleChange(value) {
      this.num = value;
      console.log(this.num)
    },
    handleClick(tab, event) {
      console.log(tab, event);
    },
    collect(){
      const data = {goodsId: this.goods.id}
      this.$request.post('/collect/add',data).then(res => {
        if (res.code == '200'){
          this.$notify.success({title: '完了', message: 'お気に入りに追加しました', showClose: false, duration: 2000});
          this.isCollect = true
        } else {
          this.$notify.error({title: 'エラー', message: res.msg, showClose: false, duration: 2000});
          this.isCollect = false
        }
        this.loadGoods()
      })
    },
    buy(){
      const data = {
        name: this.goods.name,
        goodsId: this.goods.id,
        price: this.goods.price * this.num,
        nums: this.num,
        userName: this.user.username,
        userPhone: this.user.phone,
        userAddress: this.user.address,
        userId: this.user.id
      }
      this.$request.post('/orders/add',data).then(res => {
        if (res.code == '200'){
          this.$notify.success({title: '完了', message: '注文を作成しました。お早めにお支払いください', showClose: false, duration: 2000});
          this.$router.push('/front/orders')
        } else {
          this.$notify.error({title: 'エラー', message: res.msg, showClose: false, duration: 2000});
        }
      })
    },
    computed: {
      summaryHtml() {
        const summary = this.goods.content ? this.stripHtml(this.goods.content) : this.goods.descr
        return summary ? '<p>' + this.escapeHtml(summary) + '</p>' : ''
      }
    }
  }
}
</script>

<style scoped>
.goods-detail-page {
  width: 60%;
  min-height: 90vh;
  margin: 10px auto;
}

.publisher-detail {
  margin: 14px 15px 0;
  color: #606266;
  font-size: 12px;
}

.goods-summary {
  padding: 0 15px;
  color: #606266;
  font-size: 12px;
  line-height: 1.7;
}

.goods-summary >>> img,
.goods-content-detail >>> img {
  max-width: 100%;
  height: auto;
}

</style>