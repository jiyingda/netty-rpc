/**
 * @(#)Param.java, 10月 21, 2022.
 * <p>
 * Copyright 2022 . All rights reserved.
 *  PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.cpf.nettyrpc.common;

import java.io.Serializable;

/**
 * @author jiyingdabj
 */
public class Param implements Serializable {

    private static final long serialVersionUID = 278592291450011396L;

    private Class<?>[] classType;

    private Object[] objects;

}
