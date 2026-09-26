<template>
  <div class="form-container">
    <section class="account-sections">
      <el-card class="account-card">
        <el-tabs v-model="activeSection" @tab-click="handleSectionChange">
          <el-tab-pane :label="$t('common.myProducts')" name="products">
            <el-row v-if="myGoods.length" :gutter="18">
              <el-col :xs="12" :sm="8" :md="6" v-for="item in myGoods" :key="item.id" class="activity-col">
                <el-card class="activity-card" :body-style="{ padding: '0px' }" @click.native="goGoods(item.id)">
                  <img :src="getImageUrl(item.cover)" class="activity-image" @error="handleImageError">
                  <div class="activity-content">
                    <div class="activity-name">{{ item.name }}</div>
                    <div class="activity-meta">{{ item.price }}円</div>
                    <div class="activity-publisher">{{ $t('common.publisher') }}：{{ item.userName || user.name }}</div>
                    <div class="activity-actions">
                      <el-button type="primary" size="mini" plain @click.stop="openEditGoods(item)">{{ $t('common.editProduct') }}</el-button>
                      <el-button type="danger" size="mini" plain @click.stop="deleteGoods(item)">{{ $t('common.deleteProduct') }}</el-button>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            <el-empty v-else :description="$t('common.noMyProducts')"></el-empty>
          </el-tab-pane>

          <el-tab-pane :label="$t('common.myNotices')" name="notices">
            <el-table v-if="myNotices.length" :data="myNotices" stripe>
              <el-table-column prop="name" :label="$t('common.title')" min-width="220"></el-table-column>
              <el-table-column :label="$t('common.content')" min-width="300" show-overflow-tooltip>
                <template v-slot="scope">{{ stripHtml(scope.row.content) }}</template>
              </el-table-column>
              <el-table-column prop="time" :label="$t('common.publishTime')" width="180"></el-table-column>
              <el-table-column :label="$t('common.operation')" width="100">
                <template v-slot="scope">
                  <el-button type="primary" size="mini" plain @click="openEditNotice(scope.row)">{{ $t('common.editNotice') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else :description="$t('common.noMyNotices')"></el-empty>
          </el-tab-pane>

          <el-tab-pane :label="$t('common.collection')" name="collect">
            <el-row v-if="collects.length" :gutter="18">
              <el-col :xs="12" :sm="8" :md="6" v-for="item in collects" :key="item.id" class="activity-col">
                <el-card class="activity-card" :body-style="{ padding: '0px' }" @click.native="goGoods(item.goodsId)">
                  <img :src="getImageUrl(item.goods && item.goods.cover)" class="activity-image" @error="handleImageError">
                  <div class="activity-content">
                    <div class="activity-name">{{ item.goods && item.goods.name }}</div>
                    <div class="activity-meta">{{ item.goods && item.goods.price }}円</div>
                    <el-button type="text" class="remove-collect" @click.stop="removeCollect(item)">{{ $t('common.cancelCollection') }}</el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            <el-empty v-else :description="$t('common.noCollection')"></el-empty>
          </el-tab-pane>

          <el-tab-pane :label="$t('common.unpaid')" name="unpaid">
            <el-table v-if="unpaidOrders.length" :data="unpaidOrders" stripe>
              <el-table-column prop="name" :label="$t('common.productName')" min-width="160"></el-table-column>
              <el-table-column prop="price" :label="$t('common.price')" width="100"></el-table-column>
              <el-table-column prop="nums" :label="$t('common.quantity')" width="80"></el-table-column>
              <el-table-column prop="time" :label="$t('common.orderTime')" min-width="160"></el-table-column>
              <el-table-column :label="$t('common.operation')" width="100">
                <template v-slot="scope">
                  <el-button type="primary" size="mini" plain @click="payOrder(scope.row)">{{ $t('common.pay') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else :description="$t('common.noUnpaid')"></el-empty>
          </el-tab-pane>

          <el-tab-pane :label="$t('common.purchased')" name="purchased">
            <el-table v-if="paidOrders.length" :data="paidOrders" stripe>
              <el-table-column prop="name" :label="$t('common.productName')" min-width="180"></el-table-column>
              <el-table-column prop="price" :label="$t('common.price')" width="100"></el-table-column>
              <el-table-column prop="nums" :label="$t('common.quantity')" width="80"></el-table-column>
              <el-table-column prop="time" :label="$t('common.orderTime')" min-width="160"></el-table-column>
              <el-table-column :label="$t('common.status')" width="100">
                <template v-slot="scope">
                  <el-tag type="success">{{ scope.row.state }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else :description="$t('common.noPurchased')"></el-empty>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </section>

    <el-dialog :title="$t('common.editProduct')" :visible.sync="editDialogVisible" width="560px" :close-on-click-modal="false">
      <el-form ref="editGoodsForm" :model="editGoodsForm" :rules="goodsRules" label-width="90px">
        <el-form-item :label="$t('common.productName')" prop="name">
          <el-input v-model="editGoodsForm.name" maxlength="100"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.productCategory')" prop="typeId">
          <el-select v-model="editGoodsForm.typeId" style="width: 100%">
            <el-option v-for="type in types" :key="type.id" :label="type.name" :value="type.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('common.price')" prop="price">
          <el-input-number v-model="editGoodsForm.price" :min="0.01" :precision="2" :step="1" controls-position="right" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('common.stock')" prop="store">
          <el-input-number v-model="editGoodsForm.store" :min="1" :step="1" controls-position="right" style="width: 100%"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('common.productImage')" prop="cover">
          <el-upload
            action=""
            :http-request="uploadEditCover"
            :show-file-list="false"
            :before-upload="beforeCoverUpload">
            <img v-if="editGoodsForm.cover" :src="getImageUrl(editGoodsForm.cover)" class="edit-cover-preview">
            <i v-else class="el-icon-plus edit-cover-uploader"></i>
          </el-upload>
        </el-form-item>
        <el-form-item :label="$t('common.description')" prop="descr">
          <div class="goods-editor">
            <Toolbar :editor="editor" :defaultConfig="toolbarConfig" mode="default" />
            <Editor
              v-model="editGoodsForm.content"
              :defaultConfig="editorConfig"
              mode="default"
              @onCreated="onEditorCreated"
            />
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="editDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitEditGoods">{{ $t('common.republishProduct') }}</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="$t('common.editNotice')" :visible.sync="noticeDialogVisible" width="460px" :close-on-click-modal="false">
      <el-form ref="editNoticeForm" :model="editNoticeForm" :rules="noticeRules" label-width="80px">
        <el-form-item :label="$t('common.title')" prop="name">
          <el-input v-model="editNoticeForm.name" maxlength="100"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.content')" prop="content">
          <div class="notice-editor">
            <Toolbar :editor="noticeEditor" :defaultConfig="toolbarConfig" mode="default" />
            <Editor
              v-model="editNoticeForm.content"
              :defaultConfig="editorConfig"
              mode="default"
              @onCreated="onNoticeEditorCreated"
            />
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="noticeDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitEditNotice">{{ $t('common.save') }}</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'

export default {
  name: "Person",
  components: { Editor, Toolbar },
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      activeSection: 'products',
      myGoods: [],
      myNotices: [],
      collects: [],
      orders: [],
      types: [],
      editDialogVisible: false,
      editGoodsForm: {},
      noticeDialogVisible: false,
      editNoticeForm: {},
      editor: null,
      noticeEditor: null,
      toolbarConfig: {},
      editorConfig: {
        placeholder: '请输入商品说明',
        MENU_CONF: {
          uploadImage: {
            server: '',
            fieldName: 'file',
            headers: {}
          }
        }
      },
      goodsRules: {
        name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
        typeId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
        price: [{ required: true, message: '请输入商品价格', trigger: 'change' }]
      },
      noticeRules: {
        name: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.configureEditor()
    if (!this.user.id || !this.user.token) {
      this.$router.push('/login')
      return
    }
    this.loadActivity()
  },
  beforeDestroy() {
    if (this.editor) this.editor.destroy()
    if (this.noticeEditor) this.noticeEditor.destroy()
  },
  methods: {
    configureEditor() {
      this.editorConfig.MENU_CONF.uploadImage.server = this.$baseUrl + '/file/editor/upload'
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      this.editorConfig.MENU_CONF.uploadImage.headers = { token: user.token || '' }
    },
    loadActivity() {
      this.loadTypes()
      this.loadMyGoods()
      this.loadMyNotices()
      this.loadCollects()
      this.loadOrders()
    },
    loadTypes() {
      this.$request.get('/type/selectAll').then(res => {
        this.types = Array.isArray(res.data) ? res.data : []
      })
    },
    openEditGoods(item) {
      this.editGoodsForm = {
        id: item.id,
        name: item.name,
        typeId: item.typeId,
        price: item.price,
        store: Math.max(1, item.store || 1),
        cover: item.cover,
        descr: item.descr || '',
        content: item.content || ''
      }
      this.editDialogVisible = true
      this.$nextTick(() => this.$refs.editGoodsForm && this.$refs.editGoodsForm.clearValidate())
    },
    submitEditGoods() {
      this.$refs.editGoodsForm.validate(valid => {
        if (!valid) return
        const form = Object.assign({}, this.editGoodsForm, {
          descr: this.stripHtml(this.editGoodsForm.content) || this.editGoodsForm.descr
        })
        this.$request.put('/goods/update', form).then(res => {
          if (res.code === '200') {
            this.$message.success(this.$t('common.republishSuccess'))
            this.editDialogVisible = false
            this.loadMyGoods()
          } else {
            this.$message.error(res.msg || this.$t('common.republishFailed'))
          }
        })
      })
    },
    beforeCoverUpload(file) {
      const isImage = /^image\//.test(file.type)
      if (!isImage) this.$message.error(this.$t('common.imageOnly'))
      return isImage
    },
    uploadEditCover(options) {
      const formData = new FormData()
      formData.append('file', options.file)
      this.$request.post('/file/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      }).then(res => {
        if (res.code === '200') {
          this.editGoodsForm.cover = res.data
          options.onSuccess(res)
        } else {
          options.onError(new Error(res.msg || this.$t('common.imageUploadFailed')))
        }
      }).catch(options.onError)
    },
    onEditorCreated(editor) {
      this.editor = Object.seal(editor)
    },
    onNoticeEditorCreated(editor) {
      this.noticeEditor = Object.seal(editor)
    },
    stripHtml(value) {
      if (!value) return ''
      const container = document.createElement('div')
      container.innerHTML = value
      return (container.textContent || container.innerText || '').replace(/\s+/g, ' ').trim().slice(0, 300)
    },
    deleteGoods(item) {
      this.$confirm(this.$t('common.confirmDeleteProduct'), this.$t('common.deleteProduct'), { type: 'warning' }).then(() => {
        this.$request.delete('/goods/delete?id=' + item.id).then(res => {
          if (res.code === '200') {
            this.$message.success(this.$t('common.deleteSuccess'))
            this.loadMyGoods()
          } else {
            this.$message.error(res.msg || this.$t('common.deleteFailed'))
          }
        })
      }).catch(() => {})
    },
    loadMyGoods() {
      this.$request.get('/goods/myGoods').then(res => {
        this.myGoods = Array.isArray(res.data) ? res.data : []
      }).catch(() => {
        this.myGoods = []
      })
    },
    loadMyNotices() {
      this.$request.get('/notice/myNotices').then(res => {
        this.myNotices = Array.isArray(res.data) ? res.data : []
      }).catch(() => {
        this.myNotices = []
      })
    },
    openEditNotice(item) {
      this.editNoticeForm = {
        id: item.id,
        name: item.name || '',
        content: item.content || ''
      }
      this.noticeDialogVisible = true
      this.$nextTick(() => this.$refs.editNoticeForm && this.$refs.editNoticeForm.clearValidate())
    },
    submitEditNotice() {
      this.$refs.editNoticeForm.validate(valid => {
        if (!valid) return
        if (!this.stripHtml(this.editNoticeForm.content)) {
          this.$message.error(this.$t('common.content'))
          return
        }
        this.$request.put('/notice/update', this.editNoticeForm).then(res => {
          if (res.code === '200') {
            this.$message.success(this.$t('common.noticeUpdateSuccess'))
            this.noticeDialogVisible = false
            this.loadMyNotices()
          } else {
            this.$message.error(res.msg || this.$t('common.noticeUpdateFailed'))
          }
        })
      })
    },
    loadCollects() {
      this.$request.get('/collect/myCollect').then(res => {
        this.collects = Array.isArray(res.data) ? res.data : []
      })
    },
    loadOrders() {
      this.$request.get('/orders/selectPage', {
        params: { pageNum: 1, pageSize: 100, name: '', orderNo: '' }
      }).then(res => {
        this.orders = res.data && Array.isArray(res.data.records) ? res.data.records : []
      })
    },
    handleSectionChange() {
      this.loadActivity()
    },
    goGoods(id) {
      if (id) this.$router.push({ name: 'GoodsDetail', query: { id } })
    },
    removeCollect(item) {
      this.$request.delete('/collect/delete?id=' + item.id).then(res => {
        if (res.code === '200') {
          this.$message.success(this.$t('common.cancelCollection'))
          this.loadCollects()
        } else {
          this.$message.error(res.msg || this.$t('common.cancelCollection'))
        }
      })
    },
    payOrder(order) {
      this.$confirm(this.$t('common.confirmPay'), this.$t('common.pay'), { type: 'warning' }).then(() => {
        this.$request.post('/orders/pay', { id: order.id }).then(res => {
          if (res.code === '200') {
            this.$message.success(this.$t('common.paySuccess'))
            this.loadOrders()
          } else {
            this.$message.error(res.msg || this.$t('common.payFailed'))
          }
        })
      }).catch(() => {})
    },
    getImageUrl(url) {
      if (!url) return require('@/assets/empty.svg')
      if (/^https?:\/\//i.test(url) || /^data:/i.test(url)) return url
      return this.$baseUrl + (url.startsWith('/') ? '' : '/') + url
    },
    handleImageError(event) {
      event.target.src = require('@/assets/empty.svg')
    }
  },
  computed: {
    unpaidOrders() {
      return this.orders.filter(order => order.state !== '已支付')
    },
    paidOrders() {
      return this.orders.filter(order => order.state === '已支付')
    }
  }
}
</script>

<style scoped>
.form-container {
  display: flex;
  justify-content: center;
  padding: 18px 10px;
  box-sizing: border-box;
}

.account-sections {
  width: min(1180px, 94%);
  margin: 0 auto 40px;
}

.account-card {
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, .8);
  box-shadow: 0 10px 25px -5px rgba(15, 23, 42, .08);
}

.activity-col {
  margin-top: 14px;
}

.activity-card {
  overflow: hidden;
  border-radius: 12px;
  cursor: pointer;
  transition: transform .2s ease, box-shadow .2s ease;
}

.activity-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 22px rgba(15, 23, 42, .12);
}

.activity-image {
  display: block;
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.activity-content {
  padding: 12px;
}

.activity-name {
  overflow: hidden;
  color: #303133;
  font-size: 14px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-meta {
  margin-top: 8px;
  color: #ff6700;
  font-weight: 600;
}

.activity-publisher {
  margin-top: 7px;
  color: #909399;
  font-size: 12px;
}

.activity-actions {
  display: flex;
  gap: 6px;
  margin-top: 10px;
}

.activity-actions .el-button {
  margin: 0;
}

.edit-cover-preview {
  display: block;
  width: 110px;
  height: 110px;
  border-radius: 8px;
  object-fit: cover;
}

.edit-cover-uploader {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 110px;
  height: 110px;
  border: 1px dashed #c0c4cc;
  border-radius: 6px;
  color: #909399;
  font-size: 24px;
}

.goods-editor {
  overflow: hidden;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.goods-editor >>> .w-e-text-container {
  min-height: 150px;
}

.notice-editor {
  overflow: hidden;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.notice-editor >>> .w-e-text-container {
  min-height: 180px;
}

.remove-collect {
  padding: 4px 0;
  color: #ea580c;
}

</style>