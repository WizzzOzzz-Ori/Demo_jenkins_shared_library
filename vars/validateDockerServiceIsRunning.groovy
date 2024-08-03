def call(){
    sh """
    if ! docker info > /dev/null 2>&1;
    then
        dockerd &
        while (! docker info > /dev/null 2>&1); do
            echo "Waiting for Docker daemon to start..."
            sleep 1
        done
    fi
    """
}