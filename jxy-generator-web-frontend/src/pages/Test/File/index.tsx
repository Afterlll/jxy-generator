import { COS_HOST } from '@/constants';
import {
  testDownloadFileUsingGet,
  testUploadFileUsingPost,
} from '@/services/backend/fileController';
import { InboxOutlined } from '@ant-design/icons';
import { Button, Card, Divider, Flex, message, Upload, UploadProps } from 'antd';
import { saveAs } from 'file-saver';
import React, { useState } from 'react';

/**
 * 文件上传下载测试页面
 * @constructor
 */
const TestFilePage: React.FC = () => {
  const { Dragger } = Upload;
  const [value, setValue] = useState<string>(''); // 文件地址

  const props: UploadProps = {
    name: 'file',
    multiple: false, // 关闭多文件上传
    maxCount: 1, // 最大上传数量（保证单文件上传）
    customRequest: async (fileObj: any) => {
      try {
        const res = await testUploadFileUsingPost({}, fileObj.file);
        fileObj.onSuccess(res.data); // 当文件正常上传成功之后进行正常提示
        setValue(res.data ?? '');
      } catch (e: any) {
        message.error('文件上传失败', e.message);
        fileObj.onError(e); // 上传失败时进行提示
      }
    },
    // 点击移除文件时的回调
    onRemove: () => {
      setValue(undefined ?? '');
    },
  };

  return (
    <Flex gap={16}>
      <Card title="文件上传">
        <Dragger {...props}>
          <p className="ant-upload-drag-icon">
            <InboxOutlined />
          </p>
          <p className="ant-upload-text">Click or drag file to this area to upload</p>
          <p className="ant-upload-hint">
            Support for a single or bulk upload. Strictly prohibited from uploading company data or
            other banned files.
          </p>
        </Dragger>
      </Card>
      <Card title="文件下载">
        <div>文件地址：{COS_HOST + value}</div>
        <Divider />
        <img alt={value} src={COS_HOST + value} height={200}></img>
        <Divider />
        <Button
          onClick={async () => {
            // 获取到下载流
            const blob = await testDownloadFileUsingGet(
              {
                filePath: value,
              },
              // 响应类型设置位 blob 流对象
              {
                responseType: 'blob',
              },
            );
            // 使用 file-saver 来保存文件
            const fullPath = COS_HOST + value;
            saveAs(blob, fullPath.substring(fullPath.lastIndexOf('/') + 1));
          }}
        >
          点击下载文件
        </Button>
      </Card>
    </Flex>
  );
};

export default TestFilePage;
