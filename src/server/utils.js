const responseWithError = (res, error) => {
    res.status(500)
        .json({
            success: false,
            message: error.message || error,
            data: error
        })
}

const responseWithSuccess = (res, message, data) => {
    res.json({
        success: true,
        message: message || '成功',
        data: data
    })
}

export {responseWithError, responseWithSuccess};
