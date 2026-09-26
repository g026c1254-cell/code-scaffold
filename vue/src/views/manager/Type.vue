<template>
  <div>
    <el-card>
      <div style="margin-bottom: 10px">
        <el-input style="width: 200px;margin: 0 5px" placeholder="検索..." v-model="name"></el-input>
        <el-button type="success" plain @click="load(1)">検索</el-button>
        <el-button type="info" plain @click="reset">リセット</el-button>
        <el-button type="primary" plain @click="handleAdd">追加</el-button>
      </div>
      <el-table :data="tableData" stripe>
        <el-table-column prop="id" label="番号" width="70" align="center" >
          <template slot-scope='scope'>
            <span>{{ (pageNum - 1) * pageSize + (scope.$index + 1) }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" prop="name" label="カテゴリ名"></el-table-column>

        <el-table-column label="操作" width="240" align="center">
          <template slot-scope="scope">
            <el-button plain type="success" @click="detail(scope.row)" size="mini">詳細</el-button>
            <el-button plain type="primary" @click="handleEdit(scope.row)" size="mini">編集</el-button>
            <el-button plain type="danger" size="mini" @click=del(scope.row.id)>削除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin: 10px 0">
        <el-pagination
            style="padding: 0"
            background
            layout="total, prev, pager, next"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-size="pageSize"
            :total="total">
        </el-pagination>
      </div>
    </el-card>

    <el-dialog title="情報" :visible.sync="dialogFormVisible" width="30%" :close-on-click-modal="false">
      <el-form label-width="100px" style="padding-right: 40px" :model="form" :rules="rules" ref="ruleForm">
        <el-form-item prop="name" label="カテゴリ名">
          <el-input v-model="form.name" autocomplete="off"></el-input>
        </el-form-item>

      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">キャンセル</el-button>
        <el-button type="primary" @click="save">確定</el-button>
      </div>
    </el-dialog>

    <el-drawer :visible.sync="drawerFormVisible" title="詳細" :with-header="false">
      <div class="drawer-header">
        <span class="drawer-title">詳細</span>
        <div class="drawer-actions">
          <el-tooltip placement="top" :content="isFullscreen ? '全画面を終了' : '全画面'">
            <el-button icon="el-icon-full-screen" size="mini" circle @click="toggleFullscreen"/>
          </el-tooltip>
          <el-button icon="el-icon-close" size="mini" circle @click="drawerFormVisible = false"/>
        </div>
      </div>

      <div class="drawer-content" ref="drawerContent">
        <el-form label-width="100px" style="padding-right: 40px" :model="form">
          <el-form-item prop="name" label="カテゴリ名">
            <div>{{form.name}}</div>
          </el-form-item>
        </el-form>
      </div>

      <div class="drawer-footer">
        <el-button @click="drawerFormVisible = false">閉じる</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script>
export default {
  name: "Type",
  data() {
    return {
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      name: "",
      form: {},
      dialogFormVisible: false,
      drawerFormVisible: false,
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      rules: {
        name: [
          { required: true, message: '必須項目を入力してください', trigger: 'blur'}
        ],
      },
      isFullscreen: false,
      drawerWidth: '50%',
      drawerPosition: 'right'
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      this.$request.get("/type/selectPage", {
        params: {
          pageNum: this.pageNum,
          pageSize: 10,
          name: this.name,
        }
      }).then(res => {
        this.tableData = res.data?.records
        this.total = res.data?.total
      })
    },
    save() {
      this.$refs['ruleForm'].validate((valid) => {
        if (valid) {
          this.$request({
            method: this.form.id ? 'PUT' : 'POST',
            url: this.form.id ? '/type/update' : '/type/add',
            data: this.form
          }).then(res => {
            if (res.code === '200') {
              this.$notify.success({title: '完了', message: '操作が完了しました', showClose: false, duration: 2000});
              this.dialogFormVisible = false
              this.load()
            } else {
              this.$notify.error({title: 'エラー', message: res.msg, showClose: false, duration: 2000});
            }
          })
        }
      })
    },
    handleAdd() {
      this.dialogFormVisible = true
      this.form = {}
      this.$nextTick(() => {
        if(this.$refs.cover) {
          this.$refs.cover.clearFiles();
        }
        if(this.$refs.file) {
          this.$refs.file.clearFiles();
        }
      })
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.dialogFormVisible = true
      this.$nextTick(() => {
        if(this.$refs.cover) {
          this.$refs.cover.clearFiles();
        }
        if(this.$refs.file) {
          this.$refs.file.clearFiles();
        }
      })
    },
    detail(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.drawerFormVisible = true
    },
    del(id) {
      this.$confirm('これらのデータを削除しますか？', '削除確認', {type: "warning"}).then(response => {
        this.$request.delete("/type/delete?id=" + id).then(res => {
          if (res.code === '200') {
            this.$notify.success({title: '完了', message: '操作が完了しました', showClose: false, duration: 2000});
            this.load()
          } else {
            this.$notify.error({title: 'エラー', message: res.msg, showClose: false, duration: 2000});
          }
        })
      }).catch(() => {})
    },
    reset() {
      this.name = ""
      this.load()
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },
    handleFullscreenChange() {
      if (!document.fullscreenElement && this.isFullscreen) {
        this.isFullscreen = false;
        this.drawerWidth = this.originalWidth;
        this.drawerPosition = this.originalPosition;
      }
    },
    toggleFullscreen() {
      if (!this.isFullscreen) {
        this.originalWidth = this.drawerWidth;
        this.originalPosition = this.drawerPosition;
        this.isFullscreen = true;
        this.drawerWidth = '100%';
        this.drawerPosition = 'bottom';
        this.$nextTick(() => {
          this.requestFullscreen(this.$refs.drawerContent);
        });
      } else {
        this.isFullscreen = false;
        this.drawerWidth = this.originalWidth;
        this.drawerPosition = this.originalPosition;
        this.exitFullscreen();
      }
    },
    requestFullscreen(element) {
      if (element.requestFullscreen) {
        element.requestFullscreen();
      } else if (element.webkitRequestFullscreen) {
        element.webkitRequestFullscreen();
      } else if (element.mozRequestFullScreen) {
        element.mozRequestFullScreen();
      } else if (element.msRequestFullscreen) {
        element.msRequestFullscreen();
      }
    },
    exitFullscreen() {
      if (document.exitFullscreen) {
        document.exitFullscreen();
      } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
      } else if (document.mozCancelFullScreen) {
        document.mozCancelFullScreen();
      } else if (document.msExitFullscreen) {
        document.msExitFullscreen();
      }
    }
  },
  mounted() {
    document.addEventListener('fullscreenchange', this.handleFullscreenChange);
    document.addEventListener('webkitfullscreenchange', this.handleFullscreenChange);
    document.addEventListener('mozfullscreenchange', this.handleFullscreenChange);
    document.addEventListener('MSFullscreenChange', this.handleFullscreenChange);
  },
  beforeDestroy() {
    document.removeEventListener('fullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('webkitfullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('mozfullscreenchange', this.handleFullscreenChange);
    document.removeEventListener('MSFullscreenChange', this.handleFullscreenChange);
  },
}
</script>

<style scoped>

</style>