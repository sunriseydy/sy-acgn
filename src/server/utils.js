const responseWithError = (res, error) => {
    res.status(500)
        .json({
            success: false,
            message: error.message || error || '失败',
            data: error
        })
}

const responseWithSuccess = (res, message, data) => {
    res.status(200)
        .json({
        success: true,
        message: message || '成功',
        data: data
    })
}

export {responseWithError, responseWithSuccess};
