<template>
  <div style="margin: 10px auto;width: 70%;min-height: 90vh">
    <el-card>
      <div style="margin-bottom: 10px">
        <el-input v-model="name" style="width: 200px; margin: 0 5px" placeholder="商品名を検索"></el-input>
        <el-input v-model="orderNo" style="width: 200px; margin: 0 5px" placeholder="注文番号を検索"></el-input>
        <el-button type="success" plain @click="load(1)">検索</el-button>
        <el-button type="info" plain @click="reset">リセット</el-button>
      </div>
      <el-table :data="tableData" stripe>
        <el-table-column prop="name" label="商品名" width="200">
          <template v-slot="scope">
            <el-link :href="'/front/goodsDetail?id=' + scope.row.goodsId" :underline="false">{{ scope.row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="商品画像">
          <template v-slot="scope">
            <el-image v-if="scope.row.goods && scope.row.goods.cover" :src="scope.row.goods.cover" style="width: 50px;height: 50px" fit="cover"></el-image>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="注文番号" width="150"></el-table-column>
        <el-table-column prop="price" label="合計金額" width="80"></el-table-column>
        <el-table-column prop="nums" label="数量" width="60"></el-table-column>
        <el-table-column prop="userName" label="氏名"></el-table-column>
        <el-table-column prop="userPhone" label="連絡先"></el-table-column>
        <el-table-column prop="userAddress" label="住所"></el-table-column>
        <el-table-column prop="time" label="購入日時"></el-table-column>
        <el-table-column label="注文ステータス">
          <template v-slot="scope">
            <el-tag :type="scope.row.state === '已支付' ? 'success' : 'warning'">
              {{ scope.row.state === '已支付' ? '支払い済み' : '未払い' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
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
      <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[2, 5, 10, 20]"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total">
      </el-pagination>
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
            this.$notify.error({message: res.msg, showClose: false, duration: 2000})
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
    }
  }
}
</script>
