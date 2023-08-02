const responseWithError = (res, error) => {
    res.status(500).send(error);
}

export {responseWithError};
