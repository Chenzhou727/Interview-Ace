package com.chen.InterviewAce.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.InterviewAce.common.ErrorCode;
import com.chen.InterviewAce.constant.CommonConstant;
import com.chen.InterviewAce.exception.BusinessException;
import com.chen.InterviewAce.exception.ThrowUtils;
import com.chen.InterviewAce.mapper.QuestionBankQuestionMapper;
import com.chen.InterviewAce.model.dto.questionBankQuestion.QuestionBankQuestionQueryRequest;
import com.chen.InterviewAce.model.entity.Question;
import com.chen.InterviewAce.model.entity.QuestionBank;
import com.chen.InterviewAce.model.entity.QuestionBankQuestion;
import com.chen.InterviewAce.model.entity.User;
import com.chen.InterviewAce.model.vo.QuestionBankQuestionVO;
import com.chen.InterviewAce.model.vo.UserVO;
import com.chen.InterviewAce.service.QuestionBankQuestionService;
import com.chen.InterviewAce.service.QuestionBankService;
import com.chen.InterviewAce.service.QuestionService;
import com.chen.InterviewAce.service.UserService;
import com.chen.InterviewAce.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.weaver.patterns.TypePatternQuestions;
import org.springframework.aop.framework.AopContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 题库题目关联服务实现
 *

*
 */
@Service
@Slf4j
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion> implements QuestionBankQuestionService {

    @Resource
    private UserService userService;

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionBankService questionBankService;


    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestionBankQuestion(QuestionBankQuestion questionBankQuestion, boolean add) {
        ThrowUtils.throwIf(questionBankQuestion == null, ErrorCode.PARAMS_ERROR);
        //校验题目和题库是否存在
        Long questionBankId = questionBankQuestion.getQuestionBankId();
        Long questionId = questionBankQuestion.getQuestionId();

        if(questionBankId !=null){
            QuestionBank questionBank = questionBankService.getById(questionBankId);
            ThrowUtils.throwIf(questionBank == null, ErrorCode.PARAMS_ERROR,"题库不存在");
        }
        if(questionId !=null){
            Question question = questionService.getById(questionId);
            ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR,"题目不存在");
        }



/*         // todo 从对象中取值
        String title = questionBankQuestion.getTitle();
        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        // todo 补充校验规则
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        } */
    }

    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        QueryWrapper<QuestionBankQuestion> queryWrapper = new QueryWrapper<>();
        if (questionBankQuestionQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = questionBankQuestionQueryRequest.getId();
        Long notId = questionBankQuestionQueryRequest.getNotId();
        String sortField = questionBankQuestionQueryRequest.getSortField();
        String sortOrder = questionBankQuestionQueryRequest.getSortOrder();
        Long questionBankId = questionBankQuestionQueryRequest.getQuestionBankId();
        Long questionId = questionBankQuestionQueryRequest.getQuestionId();
        Long userId = questionBankQuestionQueryRequest.getUserId();
        // todo 补充需要的查询条件

        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionBankId), "questionBankId", questionBankId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题库题目关联封装
     *
     * @param questionBankQuestion
     * @param request
     * @return
     */
    @Override
    public QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion, HttpServletRequest request) {
        // 对象转封装类
        QuestionBankQuestionVO questionBankQuestionVO = QuestionBankQuestionVO.objToVo(questionBankQuestion);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = questionBankQuestion.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionBankQuestionVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
/*         long questionBankQuestionId = questionBankQuestion.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<QuestionBankQuestionThumb> questionBankQuestionThumbQueryWrapper = new QueryWrapper<>();
            questionBankQuestionThumbQueryWrapper.in("questionBankQuestionId", questionBankQuestionId);
            questionBankQuestionThumbQueryWrapper.eq("userId", loginUser.getId());
            QuestionBankQuestionThumb questionBankQuestionThumb = questionBankQuestionThumbMapper.selectOne(questionBankQuestionThumbQueryWrapper);
            questionBankQuestionVO.setHasThumb(questionBankQuestionThumb != null);
            // 获取收藏
            QueryWrapper<QuestionBankQuestionFavour> questionBankQuestionFavourQueryWrapper = new QueryWrapper<>();
            questionBankQuestionFavourQueryWrapper.in("questionBankQuestionId", questionBankQuestionId);
            questionBankQuestionFavourQueryWrapper.eq("userId", loginUser.getId());
            QuestionBankQuestionFavour questionBankQuestionFavour = questionBankQuestionFavourMapper.selectOne(questionBankQuestionFavourQueryWrapper);
            questionBankQuestionVO.setHasFavour(questionBankQuestionFavour != null);
        } */
        // endregion

        return questionBankQuestionVO;
    }

    /**
     * 分页获取题库题目关联封装
     *
     * @param questionBankQuestionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> questionBankQuestionPage, HttpServletRequest request) {
        List<QuestionBankQuestion> questionBankQuestionList = questionBankQuestionPage.getRecords();
        Page<QuestionBankQuestionVO> questionBankQuestionVOPage = new Page<>(questionBankQuestionPage.getCurrent(), questionBankQuestionPage.getSize(), questionBankQuestionPage.getTotal());
        if (CollUtil.isEmpty(questionBankQuestionList)) {
            return questionBankQuestionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionBankQuestionVO> questionBankQuestionVOList = questionBankQuestionList.stream().map(questionBankQuestion -> {
            return QuestionBankQuestionVO.objToVo(questionBankQuestion);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionBankQuestionList.stream().map(QuestionBankQuestion::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
/*         Map<Long, Boolean> questionBankQuestionIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> questionBankQuestionIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> questionBankQuestionIdSet = questionBankQuestionList.stream().map(QuestionBankQuestion::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<QuestionBankQuestionThumb> questionBankQuestionThumbQueryWrapper = new QueryWrapper<>();
            questionBankQuestionThumbQueryWrapper.in("questionBankQuestionId", questionBankQuestionIdSet);
            questionBankQuestionThumbQueryWrapper.eq("userId", loginUser.getId());
            List<QuestionBankQuestionThumb> questionBankQuestionQuestionBankQuestionThumbList = questionBankQuestionThumbMapper.selectList(questionBankQuestionThumbQueryWrapper);
            questionBankQuestionQuestionBankQuestionThumbList.forEach(questionBankQuestionQuestionBankQuestionThumb -> questionBankQuestionIdHasThumbMap.put(questionBankQuestionQuestionBankQuestionThumb.getQuestionBankQuestionId(), true));
            // 获取收藏
            QueryWrapper<QuestionBankQuestionFavour> questionBankQuestionFavourQueryWrapper = new QueryWrapper<>();
            questionBankQuestionFavourQueryWrapper.in("questionBankQuestionId", questionBankQuestionIdSet);
            questionBankQuestionFavourQueryWrapper.eq("userId", loginUser.getId());
            List<QuestionBankQuestionFavour> questionBankQuestionFavourList = questionBankQuestionFavourMapper.selectList(questionBankQuestionFavourQueryWrapper);
            questionBankQuestionFavourList.forEach(questionBankQuestionFavour -> questionBankQuestionIdHasFavourMap.put(questionBankQuestionFavour.getQuestionBankQuestionId(), true));
        } */
        // 填充信息
        questionBankQuestionVOList.forEach(questionBankQuestionVO -> {
            Long userId = questionBankQuestionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionBankQuestionVO.setUser(userService.getUserVO(user));
            // questionBankQuestionVO.setHasThumb(questionBankQuestionIdHasThumbMap.getOrDefault(questionBankQuestionVO.getId(), false));
            // questionBankQuestionVO.setHasFavour(questionBankQuestionIdHasFavourMap.getOrDefault(questionBankQuestionVO.getId(), false));
        });
        // endregion

        questionBankQuestionVOPage.setRecords(questionBankQuestionVOList);
        return questionBankQuestionVOPage;
    }

    @Override
    public void batchAddQuestionsToBank(List<Long> questionIdList, Long questionBankId, User loginUser) {
        /*
        参数校验
        1.题目列表不为空，并且都在Question表中
        2.题库列表是否为空，并且在题库表中
         */
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIdList),ErrorCode.PARAMS_ERROR,"题目列表不能为空");
        ThrowUtils.throwIf(questionBankId== null || questionBankId<0,ErrorCode.PARAMS_ERROR,"题库id不合法");
        ThrowUtils.throwIf(loginUser==null,ErrorCode.NO_AUTH_ERROR);

        //检查题目id是否存在
        LambdaQueryWrapper<Question> questionLambdaQueryWrapper= Wrappers.lambdaQuery(Question.class)
                .select(Question::getId)
                .in(Question::getId, questionIdList);
        List<Long> validQuestionIdList = questionService.listObjs(questionLambdaQueryWrapper,o -> (Long)o);
        ThrowUtils.throwIf(CollUtil.isEmpty(validQuestionIdList),ErrorCode.PARAMS_ERROR,"合法的题目Id列表为空");

        /*
        检查要添加的题目哪些已经在题库中，哪些不在题库中，只添加没有在题库的
        1.检查已经添加过的
        2.从要添加的列表中过滤已经添加过的
        3.过滤后都是没有添加过的，添加
         */
        LambdaQueryWrapper<QuestionBankQuestion> in = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
                .in(QuestionBankQuestion::getQuestionId, validQuestionIdList);
        List<Long> existingIdList = this.list(in).stream().map(QuestionBankQuestion::getQuestionId).toList();
        List<Long> filtedValidQuestionIdList = validQuestionIdList.stream()
                .filter(questionId -> !existingIdList.contains(questionId))
                .collect(Collectors.toList());

        //检查题库id的是否存在
        QuestionBank validQuestionBank = questionBankService.getById(questionBankId);
        ThrowUtils.throwIf(validQuestionBank==null,ErrorCode.NOT_FOUND_ERROR);

        //使用线程池异步执行
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                20,
                60,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        List<CompletableFuture<Void>>  futures= new ArrayList<>();

        //创建题目题库关联
        int batchSize=1000;
        for(int i=0;i<=filtedValidQuestionIdList.size();i+=batchSize){
            List<Long> subList = filtedValidQuestionIdList.subList(i, Math.min(i + batchSize, filtedValidQuestionIdList.size()));
            List<QuestionBankQuestion> questionBankQuestions = subList.stream().map(questionId -> {
                QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
                questionBankQuestion.setQuestionBankId(questionBankId);
                questionBankQuestion.setUserId(loginUser.getId());
                questionBankQuestion.setQuestionId(questionId);
                return questionBankQuestion;
            }).collect(Collectors.toList());
            //调用事务方法批量添加
            //事务是基于代理对象的，当前类对象的代理对象调用事务才会生效，否则等于内部方法调用，事务失效
            QuestionBankQuestionService questionBankQuestionServiceProxy = (QuestionBankQuestionServiceImpl) AopContext.currentProxy();

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                questionBankQuestionServiceProxy.batchAddQuestionsToBankInner(questionBankQuestions);
            }, threadPoolExecutor).exceptionally(ex -> {
                log.error("批处理任务执行失败", ex);
                return null;
            });
            futures.add(future);
        }
        //等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        //关闭线程池
        threadPoolExecutor.shutdown();
    }

    /**
     * 批量添加题目到题库（内部调用）
     * @param questionBankQuestions
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddQuestionsToBankInner(List<QuestionBankQuestion> questionBankQuestions) {
        try {
            boolean result = this.saveBatch(questionBankQuestions);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
        } catch (DataIntegrityViolationException e) {
            log.error("数据库唯一键冲突或违反其他完整性约束, 错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目已存在于该题库，无法重复添加");
        } catch (DataAccessException e) {
            log.error("数据库连接问题、事务问题等导致操作失败, 错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据库操作失败");
        } catch (Exception e) {
            // 捕获其他异常，做通用处理
            log.error("添加题目到题库时发生未知错误，错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
        }
    }




    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveQuestionsFromBank(List<Long> questionIdList, Long questionBankId) {
        /*
        移除关联时只需要根据条件从关联表查询即可，查询不到说明不存在关联
         */
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIdList),ErrorCode.PARAMS_ERROR,"题目列表不能为空");
        ThrowUtils.throwIf(questionBankId== null || questionBankId<0,ErrorCode.PARAMS_ERROR,"题库id不合法");

        for(long questionId : questionIdList){
            LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                    .eq(QuestionBankQuestion::getQuestionId, questionId)
                    .eq(QuestionBankQuestion::getQuestionBankId, questionBankId);
            boolean remove = this.remove(lambdaQueryWrapper);
            ThrowUtils.throwIf(!remove,ErrorCode.OPERATION_ERROR,"从题库移除题目失败");
        }




    }

}
