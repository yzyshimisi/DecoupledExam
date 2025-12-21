# 成绩管理API说明

## 概述

成绩管理系统提供了对学生线上和线下成绩的统一管理功能。系统基于`student_grade`表实现，支持多种成绩类型，包括线上考试(ONLINE_EXAM)、线下考试(OFFLINE_EXAM)、测验(QUIZ)、作业(HOMEWORK)等。

## 数据结构

### StudentGrade实体类

| 字段名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| gradeId | Long | 否 | 成绩ID，主键，自增长 |
| studentId | Long | 是 | 学生ID，关联user表 |
| courseId | Long | 否 | 课程ID，关联edu_course表 |
| subjectId | Integer | 否 | 学科ID，关联subject表 |
| gradeType | String | 是 | 成绩类型：ONLINE_EXAM, OFFLINE_EXAM, QUIZ, HOMEWORK等 |
| sourceId | Long | 否 | 来源ID（如线上考试的exam_id） |
| gradeName | String | 是 | 成绩名称，如"期中考试"、"Python作业1" |
| score | BigDecimal | 是 | 实际得分 |
| fullScore | BigDecimal | 是 | 满分值，默认100.0 |
| teacherId | Long | 是 | 录入教师ID，关联user表 |
| recordTime | Date | 否 | 录入时间，默认当前时间 |
| remark | String | 否 | 备注信息 |

## API接口说明

所有接口均以`/api/student-grades`为前缀。

### 1. 添加成绩

**接口地址**: POST `/api/student-grades`

**请求体**(JSON格式):
```json
{
  "studentId": 1001,
  "gradeName": "期末考试",
  "gradeType": "OFFLINE_EXAM",
  "score": 92.5,
  "fullScore": 100.0,
  "teacherId": 2001,
  "courseId": 3001,
  "remark": "优秀"
}
```

**返回结果**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": null
}
```

### 2. 查询成绩

#### 2.1 根据ID查询成绩
**接口地址**: GET `/api/student-grades/{gradeId}`

**返回结果**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "gradeId": 1,
    "studentId": 1001,
    "courseId": 2001,
    "subjectId": 3001,
    "gradeType": "OFFLINE_EXAM",
    "sourceId": null,
    "gradeName": "期中考试",
    "score": 85.0,
    "fullScore": 100.0,
    "teacherId": 4001,
    "recordTime": "2023-10-01 10:00:00",
    "remark": "表现良好"
  }
}
```

#### 2.2 查询所有成绩
**接口地址**: GET `/api/student-grades`

#### 2.3 根据学生ID查询成绩
**接口地址**: GET `/api/student-grades/student/{studentId}`

#### 2.4 根据课程ID查询成绩
**接口地址**: GET `/api/student-grades/course/{courseId}`

#### 2.5 根据成绩类型查询成绩
**接口地址**: GET `/api/student-grades/type/{gradeType}`

### 3. 更新成绩

**接口地址**: PUT `/api/student-grades`

**请求体**(JSON格式):
```json
{
  "gradeId": 1,
  "studentId": 1001,
  "gradeName": "期末考试",
  "gradeType": "OFFLINE_EXAM",
  "score": 95.0,
  "fullScore": 100.0,
  "teacherId": 2001,
  "courseId": 3001,
  "remark": "非常优秀"
}
```

### 4. 删除成绩

**接口地址**: DELETE `/api/student-grades/{gradeId}`

## 成绩类型说明

系统支持以下成绩类型：
- ONLINE_EXAM: 线上考试
- OFFLINE_EXAM: 线下考试
- QUIZ: 测验
- HOMEWORK: 作业
- PROJECT: 项目
- EXPERIMENT: 实验
- ATTENDANCE: 考勤

## 使用示例

### 添加线下考试成绩
```bash
curl -X POST "http://localhost:8080/api/student-grades" \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1001,
    "gradeName": "期末考试",
    "gradeType": "OFFLINE_EXAM",
    "score": 92.5,
    "fullScore": 100.0,
    "teacherId": 2001,
    "courseId": 3001,
    "remark": "优秀"
  }'
```

### 查询学生所有成绩
```bash
curl "http://localhost:8080/api/student-grades/student/1001"
```

### 更新成绩
```bash
curl -X PUT "http://localhost:8080/api/student-grades" \
  -H "Content-Type: application/json" \
  -d '{
    "gradeId": 1,
    "score": 95.0,
    "remark": "非常优秀"
  }'
```

### 删除成绩
```bash
curl -X DELETE "http://localhost:8080/api/student-grades/1"
```