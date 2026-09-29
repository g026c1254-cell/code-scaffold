<template>
  <div class="orders-page">
    <el-card class="orders-card">
      <div class="orders-toolbar">
        <div class="search-inputs">
          <el-input v-model="name" class="search-input" clearable placeholder="商品名を検索" @keyup.enter.native="load(1)"></el-input>
          <el-input v-model="orderNo" class="search-input" clearable placeholder="注文番号を検索" @keyup.enter.native="load(1)"></el-input>
        </div>
        <div class="search-buttons">
          <el-button type="success" plain @click="load(1)">検索</el-button>
          <el-button type="info" plain @click="reset">リセット</el-button>
        </div>
      </div>

      <!-- 桌面端表格视图 (PC Table View) -->
      <div class="desktop-orders-table">
        <el-table :data="tableData" stripe>
          <el-table-column prop="name" label="商品名" min-width="180">
            <template v-slot="scope">
              <el-link :href="'/front/goodsDetail?id=' + scope.row.goodsId" :underline="false">{{ scope.row.name }}</el-link>
            </template>
          </el-table-column>
          <el-table-column label="商品画像" width="90">
            <template v-slot="scope">
              <el-image v-if="scope.row.goods && scope.row.goods.cover" :src="getImageUrl(scope.row.goods.cover)" style="width: 50px;height: 50px; border-radius: 6px;" fit="cover"></el-image>
            </template>
          </el-table-column>
          <el-table-column prop="orderNo" label="注文番号" min-width="150"></el-table-column>
          <el-table-column prop="price" label="合計金額" width="100">
            <template v-slot="scope">{{ scope.row.price }}円</template>
          </el-table-column>
          <el-table-column prop="nums" label="数量" width="70"></el-table-column>
          <el-table-column prop="userName" label="氏名" min-width="100"></el-table-column>
          <el-table-column prop="userPhone" label="連絡先" min-width="120"></el-table-column>
          <el-table-column prop="time" label="購入日時" min-width="160"></el-table-column>
          <el-table-column label="注文ステータス" width="120">
            <template v-slot="scope">
              <el-tag :type="scope.row.state === '已支付' ? 'success' : 'warning'">
                {{ scope.row.state === '已支付' ? '支払い済み' : '未払い' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template v-slot="scope">
              <el-button
                  v-if="scope.row.state !== '已支付'"
                  size="mini"
                  type="primary"
                  plain
                  @click="pay(scope.row)">
                支払う
              </el-button>
              <el-button size="mini" type="danger" plain @click="del(scope.row.id)">削除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 移动端卡片视图 (Mobile Card View) -->
      <div class="mobile-orders-list">
        <div v-if="tableData.length">
          <div v-for="order in tableData" :key="order.id" class="mobile-order-item">
            <div class="mobile-order-header">
              <span class="order-no">No: {{ order.orderNo }}</span>
              <el-tag size="mini" :type="order.state === '已支付' ? 'success' : 'warning'">
                {{ order.state === '已支付' ? '支払い済み' : '未払い' }}
              </el-tag>
            </div>
            <div class="mobile-order-body" @click="$router.push('/front/goodsDetail?id=' + order.goodsId)">
              <img v-if="order.goods && order.goods.cover" :src="getImageUrl(order.goods.cover)" class="order-goods-thumb" />
              <div class="order-goods-info">
                <div class="order-goods-title">{{ order.name }}</div>
                <div class="order-goods-meta">
                  <span class="order-price">{{ order.price }}円</span>
                  <span class="order-nums">x{{ order.nums }}</span>
                </div>
                <div class="order-time">{{ order.time }}</div>
              </div>
            </div>
            <div class="mobile-order-footer">
              <el-button size="small" type="danger" plain @click="del(order.id)">削除</el-button>
              <el-button
                v-if="order.state !== '已支付'"
                size="small"
                type="primary"
                @click="pay(order)">
                支払う
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-else :description="'注文がありません'"></el-empty>
      </div>

      <div class="pagination-wrapper">
        <el-pagination
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[5, 10, 20]"
            :page-size="pageSize"
            layout="total, prev, pager, next"
            :total="total">
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Orders',
  data() {
    return {
      tableData: [],
      pageNum: 1,
      pageSize: 10,
      name: '',
      orderNo: '',
      total: 0
    }
  },
  created() {
    this.load()
  },
  methods: {
    load(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/orders/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
          orderNo: this.orderNo
        }
      }).then(res => {
        this.tableData = res.data && res.data.records ? res.data.records : []
        this.total = res.data && res.data.total ? res.data.total : 0
      })
    },
    reset() {
      this.name = ''
      this.orderNo = ''
      this.load(1)
    },
    del(id) {
      this.$confirm('削除してもよろしいですか？', '削除確認', {type: 'warning'}).then(() => {
        this.$request.delete('/orders/delete?id=' + id).then(res => {
          if (res.code === '200') {
            this.$notify.success({title: '完了', message: '操作が完了しました', showClose: false, duration: 2000})
            this.load(1)
          } else {
            this.$notify.error({message: res.msg, showClose: false, duration: 2000})
          }
        })
      }).catch(() => {})
    },
    pay(order) {
      this.$confirm('この注文を支払いますか？', '注文の支払い', {type: 'warning'}).then(() => {
        this.$request.post('/orders/pay', {id: order.id}).then(res => {
          if (res.code === '200') {
            this.$notify.success({title: '完了', message: '支払いが完了しました', showClose: false, duration: 2000})
            this.load()
          } else {
            let msg = res.msg || '支払いに失敗しました'
            if (msg.indexOf('余额不足') !== -1 || msg.indexOf('残高が不足') !== -1) {
              msg = '残高が不足しています。チャージしてください'
            }
            this.$notify.error({message: msg, showClose: false, duration: 2000})
          }
        })
      }).catch(() => {})
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load(1)
    },
    getImageUrl(url) {
      if (!url) return require('@/assets/empty.svg')
      if (/^data:/i.test(url) || /^https?:/i.test(url)) return url
      return this.$baseUrl + (url.startsWith('/') ? '' : '/') + url
    }
  }
}
</script>

<style scoped>
.orders-page {
  width: min(1180px, 94%);
  min-height: 90vh;
  margin: 20px auto 40px;
}

.orders-card {
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, .8);
  box-shadow: 0 10px 25px -5px rgba(15, 23, 42, .08);
}

.orders-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-inputs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.search-input {
  width: 220px;
}

.search-buttons {
  display: flex;
  gap: 8px;
}

.desktop-orders-table {
  display: block;
}

.mobile-orders-list {
  display: none;
}

.pagination-wrapper {
  margin-top: 24px;
  text-align: right;
}

@media (max-width: 768px) {
  .orders-page {
    width: auto;
    margin: 12px 10px 28px;
  }

  .orders-card >>> .el-card__body {
    padding: 14px 12px;
  }

  .orders-toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
    margin-bottom: 14px;
  }

  .search-inputs {
    flex-direction: column;
    width: 100%;
  }

  .search-input {
    width: 100% !important;
  }

  .search-buttons {
    width: 100%;
  }

  .search-buttons .el-button {
    flex: 1;
    margin-left: 0 !important;
  }

  /* 切换视图：隐藏 PC Table，显示 Mobile Card */
  .desktop-orders-table {
    display: none;
  }

  .mobile-orders-list {
    display: block;
  }

  .mobile-order-item {
    background: #fff;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    padding: 14px;
    margin-bottom: 12px;
    box-shadow: 0 2px 6px rgba(15, 23, 42, .04);
  }

  .mobile-order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 10px;
    border-bottom: 1px dashed #f1f5f9;
  }

  .order-no {
    font-size: 12px;
    color: #64748b;
    word-break: break-all;
  }

  .mobile-order-body {
    display: flex;
    gap: 12px;
    padding: 12px 0;
    cursor: pointer;
  }

  .order-goods-thumb {
    width: 72px;
    height: 72px;
    border-radius: 8px;
    object-fit: cover;
    background: #f8fafc;
    flex-shrink: 0;
  }

  .order-goods-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .order-goods-title {
    font-size: 14px;
    font-weight: 600;
    color: #334155;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .order-goods-meta {
    display: flex;
    justify-content: space-between;
    align-items: baseline;
    margin-top: 6px;
  }

  .order-price {
    font-size: 16px;
    font-weight: 700;
    color: #ff7e29;
  }

  .order-nums {
    font-size: 13px;
    color: #94a3b8;
  }

  .order-time {
    font-size: 11px;
    color: #94a3b8;
    margin-top: 4px;
  }

  .mobile-order-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding-top: 10px;
    border-top: 1px solid #f8fafc;
  }

  .mobile-order-footer .el-button {
    min-width: 80px;
    margin-left: 0 !important;
  }

  .pagination-wrapper {
    text-align: center;
  }

  .pagination-wrapper >>> .el-pagination__total,
  .pagination-wrapper >>> .el-pagination__jump {
    display: none;
  }
}
</style>
