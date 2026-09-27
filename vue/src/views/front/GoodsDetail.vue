<template>
  <div class="goods-detail-page">
    <div class="product-overview">
      <div class="product-image-wrap">
        <el-image v-if="goods.cover" class="product-image" :src="goods.cover" fit="contain" :preview-src-list="[goods.cover]"></el-image>
      </div>
      <div class="product-info">
        <div class="product-title">
          <b>{{goods.name}}</b>
        </div>
        <div class="product-summary-wrap">
          <div class="goods-summary" v-html="summaryHtml"></div>
          <div class="summary-divider"></div>
        </div>
        <div class="product-meta">
          <div class="price-block">
            <span class="meta-label">{{ $t('common.price') }}：</span>
            <span class="product-price">{{goods.price}}円</span>
          </div>
          <div class="stock-block">
            <span class="meta-label">{{ $t('common.stock') }}：{{goods.store}}</span>
          </div>
        </div>
        <div class="publisher-detail">{{ $t('common.publisher') }}：{{ goods.userName || $t('common.anonymous') }}</div>
        <div class="listed-date">
          <span>{{ $t('common.listedAt') }}：{{goods.date}}</span>
        </div>
        <div class="purchase-actions">
          <el-input-number v-model="num" @change="handleChange" :min="1" :max="Math.max(1, Math.min(10, Number(goods.store) || 1))" :label="$t('common.stock')"></el-input-number>
          <el-button type="primary" class="purchase-button" @click="buy">{{ $t('common.buyNow') }}</el-button>
          <el-button :type="isCollect ? 'danger' : 'warning'" class="favorite-button" @click="collect">{{isCollect ? $t('common.favorited') : $t('common.favorite')}}</el-button>
        </div>
      </div>
    </div>

    <div class="product-details">
      <el-card class="details-card">
        <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
          <el-tab-pane :label="$t('common.detailIntroduction')" name="goods">
            <div class="w-e-text goods-content-detail" v-html="parsedDetails.detailHtml || summaryHtml"></div>
          </el-tab-pane>
          <el-tab-pane :label="$t('common.purchaseNotice')" name="notice">
            <div class="purchase-notice">
              <!-- 出品者からの注意事項 -->
              <div v-if="parsedDetails.purchaseNotice" class="seller-notice-box">
                <h3 class="notice-subhead"><i class="el-icon-warning-outline"></i> 出品者からの購入・取引注意事項</h3>
                <div class="seller-notice-text">{{ parsedDetails.purchaseNotice }}</div>
              </div>

              <!-- 全学プラットフォーム共通の利用ガイド -->
              <h3 class="notice-subhead" :style="{ marginTop: parsedDetails.purchaseNotice ? '20px' : '0' }">
                <i class="el-icon-info"></i> {{ $t('common.purchaseGuide') }}
              </h3>
              <div class="platform-notice-list">
                <div>1、{{ $t('common.genuine') }}（学生間取引の適正品）</div>
                <div>2、{{ $t('common.returnPolicy') }}（受取時の現物確認を推奨）</div>
                <div>3、{{ $t('common.freeShipping') }}（キャンパス内手渡し対応可能）</div>
                <div>4、{{ $t('common.afterSales') }}（安心の学内サポート）</div>
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
  computed: {
    parsedDetails() {
      const content = this.goods.content || ''
      const delimiter = '<!--PURCHASE_NOTICE_START-->'
      if (content.indexOf(delimiter) !== -1) {
        const parts = content.split(delimiter)
        const descHtml = parts[0].trim() || (this.goods.descr ? '<p>' + this.escapeHtml(this.goods.descr).replace(/\n/g, '<br>') + '</p>' : '')
        return {
          detailHtml: descHtml,
          purchaseNotice: parts[1].trim()
        }
      }
      return {
        detailHtml: content ? content.replace(/\n/g, '<br>') : (this.goods.descr ? '<p>' + this.escapeHtml(this.goods.descr).replace(/\n/g, '<br>') + '</p>' : ''),
        purchaseNotice: ''
      }
    },
    summaryHtml() {
      const summary = this.goods.content ? this.stripHtml(this.goods.content.split('<!--PURCHASE_NOTICE_START-->')[0]) : this.goods.descr
      return summary ? '<p>' + this.escapeHtml(summary) + '</p>' : ''
    }
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
        this.goods = res.data || {}
        this.isCollect = this.goods.isCollect || false
      })
    },
    handleChange(value) {
      this.num = value
    },
    handleClick() {},
    collect(){
      const data = { goodsId: this.goods.id }
      this.$request.post('/collect/add', data).then(res => {
        if (res.code == '200'){
          this.$notify.success({title: '完了', message: 'お気に入りに追加しました', showClose: false, duration: 2000});
          this.isCollect = true
        } else {
          this.$notify.error({title: 'エラー', message: res.msg || '失敗しました', showClose: false, duration: 2000});
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
      this.$request.post('/orders/add', data).then(res => {
        if (res.code == '200'){
          this.$notify.success({title: '完了', message: '注文を作成しました。お早めにお支払いください', showClose: false, duration: 2000});
          this.$router.push('/front/orders')
        } else {
          this.$notify.error({title: 'エラー', message: res.msg || '注文の作成に失敗しました', showClose: false, duration: 2000});
        }
      })
    }
  }
}
</script>

<style scoped>
.goods-detail-page {
  width: min(1080px, 92%);
  margin: 30px auto 40px;
}

.product-overview {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

.product-image-wrap {
  width: 440px;
  flex-shrink: 0;
}

.product-image {
  width: 100%;
  height: 380px;
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, .8);
  box-shadow: 0 4px 16px rgba(15, 23, 42, .04);
  background: #fff;
}

.product-info {
  flex: 1;
}

.product-title {
  color: #1e293b;
  font-size: 24px;
  line-height: 1.4;
  overflow-wrap: anywhere;
}

.product-summary-wrap {
  margin-top: 14px;
}

.summary-divider {
  height: 1px;
  background: #f1f5f9;
  margin: 14px 0;
}

.product-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px 24px;
  margin-top: 18px;
}

.meta-label,
.listed-date {
  color: #64748b;
  font-size: 13px;
}

.product-price {
  color: #ff7e29;
  font-size: 26px;
  font-weight: 700;
}

.publisher-detail {
  margin: 14px 0 0;
  color: #64748b;
  font-size: 13px;
  overflow-wrap: anywhere;
}

.goods-summary {
  color: #475569;
  font-size: 13px;
  line-height: 1.7;
}

.listed-date {
  margin-top: 16px;
  overflow-wrap: anywhere;
}

.purchase-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 24px;
}

.purchase-button,
.favorite-button {
  min-width: 150px;
  font-weight: 600;
  border-radius: 10px;
  transition: all .2s ease;
}

.purchase-button {
  background: linear-gradient(135deg, #ffa86b 0%, #ff7e29 100%) !important;
  border: none !important;
  box-shadow: 0 4px 14px rgba(255, 126, 41, .28);
}

.purchase-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(255, 126, 41, .38);
}

.product-details {
  margin-top: 30px;
}

.details-card {
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, .8);
}

.goods-content-detail {
  min-height: 150px;
  line-height: 1.8;
  color: #334155;
  padding: 10px 4px;
}

.seller-notice-box {
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 10px;
  padding: 16px 20px;
}

.notice-subhead {
  color: #ea6b1f;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.seller-notice-text {
  color: #431407;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
}

.platform-notice-list {
  color: #64748b;
  font-size: 13px;
  line-height: 2;
  margin-top: 8px;
}

@media (max-width: 768px) {
  .goods-detail-page {
    width: 94%;
    margin-top: 16px;
  }
  .product-overview {
    flex-direction: column;
    gap: 18px;
  }
  .product-image-wrap {
    width: 100%;
  }
  .product-image {
    height: 280px;
  }
  .purchase-actions {
    width: 100%;
  }
  .purchase-button,
  .favorite-button {
    flex: 1;
    min-width: 0;
  }
}
</style>
