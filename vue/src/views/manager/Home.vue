<template>
  <div>
    <el-row :gutter="24">
      <el-col :span="24">
        <el-card style="margin: 10px 0">
          <div style="font-size: 17px;font-weight: 700">{{ $t('nav.admin') }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="10">
      <el-col :span="12">
        <el-card class="box-card">
          <div class="clearfix" style="margin-bottom: 20px">
            <span style="font-size: 20px;font-weight: bold">{{ $t('common.notice') }}</span>
          </div>
          <div class="text item">
            <el-collapse v-model="activeNames" @change="handleChange">
              <el-collapse-item :title="item.name" :name="index" v-for="(item,index) in notices" :key="index">
                <div>{{item.content}}</div>
                <div class="publisher-tag">投稿者：{{ item.userName || '匿名ユーザー' }}</div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-card>
      </el-col>

    </el-row>
  </div>
</template>

<script>
export default {
  name: "Home",
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      activeNames: [0],
      notices: []
    }
  },
  created() {
    this.loadNotice()
  },
  methods: {
    handleChange(val) {
      console.log(val);
    },
    loadNotice(){
      this.$request.get('/notice/selectAll').then(res => {
        this.notices = Array.isArray(res.data) ? res.data : []
      }).catch(() => {
        this.notices = []
      })
    },
  }
}
</script>

<style scoped>
.text {
  font-size: 14px;
}

.item {
  margin-bottom: 18px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both
}


</style>