const responseWithError = (res, error) => {
  res.status(500).json({
    success: false,
    message: error.message || error || '失败',
    data: error,
  })
}

const responseWithSuccess = (res, message, data) => {
  res.status(200).json({
    success: true,
    message: message || '成功',
    data: data,
  })
}

const parsePageParams = (req) => {
  const page = req.query.page === '0' ? 0 : req.query.page || 1
  const size = req.query.size === '0' ? 0 : req.query.size || 10
  return {
    take: page === 0 ? undefined : Number(size),
    skip: page === 0 ? undefined : Number(page - 1) * Number(size),
  }
}

export { responseWithError, responseWithSuccess, parsePageParams }
