package com.voxlearning.utopia.service.campaign.impl.activity.twofour.base;

import java.util.Stack;

public class TwoFourUtils {

    private static String insetBlanks(String s) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char indexChar = s.charAt(i);
            if (indexChar == '(' || indexChar == ')' || indexChar == '+' || indexChar == '-' || indexChar == '*' || indexChar == '/') {
                result.append(" ").append(indexChar).append(" ");
            } else {
                result.append(indexChar);
            }
        }
        return result.toString();
    }

    public static String evaluateExpression(String expression) {
        Stack<Integer> operandStack = new Stack<>();
        Stack<Character> operatorStack = new Stack<>();
        expression = insetBlanks(expression);
        String[] tokens = expression.split(" ");
        try {
            for (String token : tokens) {
                if (token.length() == 0) {
                    continue;
                } else if (token.charAt(0) == '+' || token.charAt(0) == '-' || token.charAt(0) == '*' || token.charAt(0) == '/') {
                    while (!operatorStack.isEmpty() && (operatorStack.peek() == '-' || operatorStack.peek() == '+' || operatorStack.peek() == '/' || operatorStack.peek() == '*')) {
                        processAnOperator(operandStack, operatorStack);
                    }
                    operatorStack.push(token.charAt(0));
                } else if (token.trim().charAt(0) == '(') {
                    operatorStack.push('(');
                } else if (token.trim().charAt(0) == ')') {
                    while (operatorStack.peek() != '(') {
                        processAnOperator(operandStack, operatorStack);
                    }
                    operatorStack.pop();
                } else {
                    operandStack.push(Integer.parseInt(token));
                }
            }
            while (!operatorStack.isEmpty()) {
                processAnOperator(operandStack, operatorStack);
            }
            return operandStack.pop() + "";
        } catch (TwoFourPointException e) {
            return e.getMessage();
        }
    }


    private static void processAnOperator(Stack<Integer> operandStack, Stack<Character> operatorStack) throws TwoFourPointException {
        char op = operatorStack.pop();
        int op1 = operandStack.pop(); // 后一个
        int op2 = operandStack.pop(); //前一个

        if (op == '+') {
            operandStack.push(op1 + op2);
        } else if (op == '-') {
            int item = op2 - op1;
            if (item < 0) {
                throw new TwoFourPointException("出现负数：" + op2 + "-" + op1);
            }
            operandStack.push(item);
        } else if (op == '*') {
            operandStack.push(op1 * op2);
        } else if (op == '/') {
            if (op1 == 0) {
                throw new TwoFourPointException("除数不能为0：" + op2 + "/" + op1);
            }
            if (op2 % op1 == 0) {
                operandStack.push(op2 / op1);
            } else {
                throw new TwoFourPointException("不能整除：" + op2 + "/" + op1);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(evaluateExpression("(1+1)*(1+11)"));
        System.out.println(evaluateExpression("(1+1)*(1*12)"));
        System.out.println(evaluateExpression("(1+1)*(2*6)"));
        System.out.println(evaluateExpression("(1+1)*(2+10)"));
        System.out.println(evaluateExpression("(1*1)*(2*12)"));
        System.out.println(evaluateExpression("(1/1)*(2*12)"));
        System.out.println(evaluateExpression("(1+1)*(3*4)"));
        System.out.println(evaluateExpression("(1*1)*(3*8)"));
        System.out.println(evaluateExpression("(1/1)*(3*8)"));
        System.out.println(evaluateExpression("(1+1)*(3+9)"));
        System.out.println(evaluateExpression("(1+1)*(4*3)"));
        System.out.println(evaluateExpression("(1*1)*(4*6)"));
        System.out.println(evaluateExpression("(1/1)*(4*6)"));
        System.out.println(evaluateExpression("(1+1)*(4+8)"));
        System.out.println(evaluateExpression("(1+1)*(5+7)"));
        System.out.println(evaluateExpression("(1+1)*(6*2)"));
        System.out.println(evaluateExpression("(1*1)*(6*4)"));
        System.out.println(evaluateExpression("(1/1)*(6*4)"));
        System.out.println(evaluateExpression("(1+1)*(6+6)"));
        System.out.println(evaluateExpression("(1+1)*(7+5)"));
        System.out.println(evaluateExpression("(1*1)*(8*3)"));
        System.out.println(evaluateExpression("(1/1)*(8*3)"));
        System.out.println(evaluateExpression("(1+1)*(8+4)"));
        System.out.println(evaluateExpression("(1+1)*(9+3)"));
        System.out.println(evaluateExpression("(1+1)*(10+2)"));
        System.out.println(evaluateExpression("(1+1)*(11+1)"));
        System.out.println(evaluateExpression("(1*1)*(11+13)"));
        System.out.println(evaluateExpression("(1/1)*(11+13)"));
        System.out.println(evaluateExpression("(1+1)*(12*1)"));
        System.out.println(evaluateExpression("(1+1)*(12/1)"));
    }
}