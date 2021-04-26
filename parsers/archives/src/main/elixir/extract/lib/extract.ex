defmodule EXTRACT do
    @moduledoc """
    Extract files from archives.
    There is support for .zip, .tar and tar.gz types
    """

    baseLocation = "../../../../../../../storage"
    # TODO after adding RabbitMq support this path will be taken from queue
    sourceArchivePath =  "../../../../../../../storage/test.zip"

    pathParts = String.split(sourceArchivePath, ".")
    extension = List.last(pathParts)

    penultimate = Enum.at(pathParts, length(pathParts) - 2)
    extension = if extension == "gz" and penultimate == "tar" do
            "tar.gz"
        else
            extension
    end

    baseLocation = to_charlist(baseLocation)
    sourceArchivePath = to_charlist(sourceArchivePath)


    IO.inspect("Starting extraction of file: #{sourceArchivePath}...")

    case extension do
        "zip" ->
            case :zip.unzip(sourceArchivePath, [{:cwd, baseLocation}, :verbose]) do
                {:ok, _content} ->
                    IO.inspect("Successfully extracted ZIP archive:")
                {:error, _error} ->
                    IO.inspect("Cannot extract ZIP archive:")
            end
        "tar" ->
             case :erl_tar.extract(sourceArchivePath, [{:cwd, baseLocation}, :verbose]) do
                 :ok ->
                     IO.inspect("Successfully extracted TAR archive")
                 {:error, {_name, :eisdir}} ->
                     IO.inspect("Cannot extract TAR archive")
            end
        "tar.gz" ->
            case :erl_tar.extract(sourceArchivePath, [{:cwd, baseLocation}, :verbose, :compressed]) do
                :ok ->
                    IO.inspect("Successfully extracted TAR.GZ archive")
                {:error, {_name, :eisdir}} ->
                    IO.inspect("Cannot extract TAR.GZ archive")
            end
        other ->
            IO.inspect("Archive with #{other} extension is unsupported")
    end

end
