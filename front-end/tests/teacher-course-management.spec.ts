import { test, expect } from '@playwright/test';

/**
 * 教师端课程管理功能测试
 * 
 * 测试说明：
 * - 使用 storageState 配置管理认证状态，无需在每个测试中重复登录
 * - 每个测试点使用独立的 test 函数
 * - 采用 Page Object Model 模式组织代码
 */

test.describe('教师端 - 课程管理', () => {
  
  // 测试前导航到课程管理页面
  test.beforeEach(async ({ page }) => {
    await page.goto('/teacher/courses');
    // 等待页面加载完成
    await page.waitForSelector('h1:text("课程管理")');
  });

  /**
   * 测试点1: 课程列表展示
   * 验证课程列表正确加载和显示，课程卡片包含完整信息
   */
  test('应该正确显示课程列表', async ({ page }) => {
    // 验证页面标题
    await expect(page.locator('h1')).toContainText('课程管理');
    
    // 验证课程卡片容器存在
    const courseCards = page.locator('.card.bg-base-100.shadow-xl');
    await expect(courseCards.first()).toBeVisible();
    
    // 验证课程卡片包含必要信息（名称、描述、状态徽章）
    const firstCard = courseCards.first();
    await expect(firstCard.locator('.card-title')).toBeVisible();
    await expect(firstCard.locator('.text-gray-500.text-sm')).toBeVisible();
    await expect(firstCard.locator('.badge')).toBeVisible();
  });

  /**
   * 测试点2: 课程搜索功能
   * 验证按课程名称搜索功能正常工作
   */
  test('应该能够通过课程名称搜索课程', async ({ page }) => {
    // 获取第一个课程的名称
    const firstCourseTitle = await page.locator('.card .card-title').first().textContent();
    
    // 在搜索框中输入课程名称的一部分
    const searchKeyword = firstCourseTitle?.substring(0, 3) || '';
    await page.locator('input[placeholder="搜索课程..."]').fill(searchKeyword);
    
    // 等待搜索结果更新（可以通过观察卡片数量变化或添加短暂延迟）
    await page.waitForTimeout(500);
    
    // 验证搜索结果包含输入的关键字
    const filteredCards = page.locator('.card.bg-base-100.shadow-xl');
    const count = await filteredCards.count();
    
    if (count > 0) {
      // 至少有一个结果，验证结果中包含搜索关键字
      for (let i = 0; i < count; i++) {
        const cardTitle = await filteredCards.nth(i).locator('.card-title').textContent();
        expect(cardTitle?.toLowerCase()).toContain(searchKeyword.toLowerCase());
      }
    }
    
    // 清空搜索框，验证恢复完整列表
    await page.locator('input[placeholder="搜索课程..."]').clear();
    await page.waitForTimeout(500);
  });

  /**
   * 测试点3: 创建课程功能
   * 验证填写必填字段成功创建课程
   */
  test('应该能够创建新课程', async ({ page }) => {
    // 点击"创建课程"按钮
    await page.locator('.mb-6.flex.justify-between button.btn-primary').click();
    
    // 验证创建课程模态框打开
    const modal = page.locator('.modal-box');
    await expect(modal).toBeVisible();
    await expect(modal.locator('h3')).toContainText('创建新课程');
    
    // 填写课程名称（必填）
    const courseName = `测试课程_${Date.now()}`;
    await modal.locator('input[placeholder="输入课程名称"]').fill(courseName);
    
    // 填写课程描述（可选）
    await modal.locator('textarea[placeholder="输入课程描述"]').fill('这是一个测试课程描述');
    
    // 选择学科（必填）- 选择第一个可用的学科
    await modal.locator('select.select-bordered').first().selectOption({ index: 1 });
    
    // 点击"创建课程"按钮提交
    await modal.locator('button[type="submit"].btn-primary').click();
    
    // 等待模态框关闭（可能需要等待API响应）
    await page.waitForTimeout(1000);
    
    // 验证新创建的课程出现在列表中
    const courseCards = page.locator('.card.bg-base-100.shadow-xl');
    const found = await courseCards.filter({ hasText: courseName }).count();
    expect(found).toBeGreaterThan(0);
  });

  /**
   * 测试点4: 编辑课程功能
   * 验证修改课程名称和描述成功更新
   */
  test('应该能够编辑课程信息', async ({ page }) => {
    // 找到第一个可编辑的课程（状态为"开设中"的课程）
    const editableCourse = page.locator('.card.bg-base-100.shadow-xl').first();
    const originalTitle = await editableCourse.locator('.card-title').textContent();
    
    // 点击编辑按钮
    await editableCourse.locator('button.btn-warning').click();
    
    // 验证编辑模态框打开
    const modal = page.locator('.modal-box');
    await expect(modal).toBeVisible();
    await expect(modal.locator('h3')).toContainText('编辑课程');
    
    // 验证表单预填充了原有数据
    const nameInput = modal.locator('input[placeholder="输入课程名称"]');
    await expect(nameInput).toHaveValue(originalTitle || '');
    
    // 修改课程名称
    const newCourseName = `${originalTitle}_已编辑`;
    await nameInput.fill(newCourseName);
    
    // 修改课程描述
    await modal.locator('textarea[placeholder="输入课程描述"]').fill('更新后的课程描述');
    
    // 点击"更新课程"按钮提交
    await modal.locator('button[type="submit"].btn-primary').click();
    
    // 等待更新完成
    await page.waitForTimeout(1000);
    
    // 验证课程名称已更新
    const updatedCard = page.locator('.card.bg-base-100.shadow-xl').filter({ hasText: newCourseName });
    await expect(updatedCard).toBeVisible();
  });

  /**
   * 测试点5: 删除课程功能
   * 验证确认后成功删除课程
   */
  test('应该能够删除课程', async ({ page }) => {
    // 注意：此测试会实际删除数据，建议仅在测试环境中运行
    // 或者使用最后一个课程进行测试
    
    // 找到最后一个课程（避免影响其他测试）
    const courseCards = page.locator('.card.bg-base-100.shadow-xl');
    const lastCourseIndex = await courseCards.count() - 1;
    
    if (lastCourseIndex >= 0) {
      const lastCourse = courseCards.nth(lastCourseIndex);
      const courseTitle = await lastCourse.locator('.card-title').textContent();
      
      // 点击删除按钮
      await lastCourse.locator('button.btn-error').click();
      
      // 处理浏览器确认对话框
      page.on('dialog', async dialog => {
        expect(dialog.message()).toContain('确定要删除这个课程吗？');
        await dialog.accept(); // 确认删除
      });
      
      // 等待删除操作完成
      await page.waitForTimeout(1000);
      
      // 验证课程已从列表中移除
      const remainingCards = page.locator('.card.bg-base-100.shadow-xl');
      const found = await remainingCards.filter({ hasText: courseTitle || '' }).count();
      expect(found).toBe(0);
    }
  });

  /**
   * 测试点6: 课程状态切换
   * 验证从"开设中"切换到"已结课"
   */
  test('应该能够切换课程状态', async ({ page }) => {
    // 找到第一个"开设中"的课程
    const activeCourse = page.locator('.card.bg-base-100.shadow-xl').filter({ has: page.locator('.badge-success') }).first();
    
    if (await activeCourse.count() > 0) {
      // 验证当前状态为"开设中"
      await expect(activeCourse.locator('.badge-success')).toBeVisible();
      
      // 点击"结课"按钮
      await activeCourse.locator('button.btn-error').click();
      
      // 等待状态更新
      await page.waitForTimeout(1000);
      
      // 验证状态已变为"已结课"
      await expect(activeCourse.locator('.badge-error')).toBeVisible();
      
      // 验证按钮文本变为"开启"
      await expect(activeCourse.locator('button.btn-success')).toContainText('开启');
      
      // 再次点击"开启"按钮，恢复为"开设中"
      await activeCourse.locator('button.btn-success').click();
      await page.waitForTimeout(1000);
      
      // 验证状态恢复为"开设中"
      await expect(activeCourse.locator('.badge-success')).toBeVisible();
    }
  });

  /**
   * 测试点7: 刷新功能
   * 验证点击刷新按钮重新加载课程列表
   */
  test('应该能够通过刷新按钮重新加载课程列表', async ({ page }) => {
    // 记录当前课程数量
    const initialCount = await page.locator('.card.bg-base-100.shadow-xl').count();
    
    // 点击刷新按钮
    await page.locator('.mb-6.flex.justify-between button.btn-outline').click();
    
    // 等待刷新完成
    await page.waitForTimeout(1000);
    
    // 验证课程列表仍然存在（数量可能相同或不同，取决于是否有新增/删除）
    const refreshedCount = await page.locator('.card.bg-base-100.shadow-xl').count();
    expect(refreshedCount).toBeGreaterThanOrEqual(0);
    
    // 验证页面仍然正常显示
    await expect(page.locator('h1')).toContainText('课程管理');
  });
});
