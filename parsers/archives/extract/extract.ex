defmodule Extract do
  @moduledoc """
  Extract files from archives.
  There is support for .zip, .tar and tar.gz types.
  """

  def extract(path) do
    base_location = "../../../storage/"
    source_archive_path =  base_location <> path

    path_parts = String.split(source_archive_path, ".")
    extension = List.last(path_parts)

    penultimate = Enum.at(path_parts, length(path_parts) - 2)
    extension = if extension == "gz" and penultimate == "tar" do
      "tar.gz"
    else
      extension
    end

    base_location = to_charlist(base_location)
    source_archive_path = to_charlist(source_archive_path)


    IO.inspect("Starting extraction of file: #{source_archive_path} ...")

    case extension do
      "zip" ->
        case :zip.unzip(source_archive_path, [{:cwd, base_location}, :verbose]) do
          {:ok, content} ->
            IO.inspect("Successfully extracted ZIP archive:")
            content
          {:error, :enoent} ->
            IO.inspect("Given ZIP archive\'s path does not exist")
          {:error, error} ->
            IO.inspect("Cannot extract ZIP archive: #{error}}")
        end
      "tar" ->
        case :erl_tar.extract(source_archive_path, [{:cwd, base_location}, :verbose]) do
          :ok ->
            {:ok, output} = :erl_tar.extract(source_archive_path, [:memory])
            names = Enum.map(output, fn name_and_content -> Kernel.elem(name_and_content, 0) end)
            IO.inspect("Successfully extracted TAR archive")
            names
          {:error, {_name, :enoent}} ->
            IO.inspect("Given TAR archive\'s path does not exist")
          {:error, {_name, :eisdir}} ->
            IO.inspect("Cannot extract TAR archive")
        end
      "tar.gz" ->
        case :erl_tar.extract(source_archive_path, [{:cwd, base_location}, :verbose, :compressed]) do
          :ok ->
            {:ok, output} = :erl_tar.extract(source_archive_path, [:memory, :compressed])
            names = Enum.map(output, fn name_and_content -> Kernel.elem(name_and_content, 0) end)
            IO.inspect("Successfully extracted TAR.GZ archive")
            names
          {:error, {_name, :enoent}} ->
            IO.inspect("Given TAR.GZ archive\'s path does not exist")
          {:error, {_name, :eisdir}} ->
            IO.inspect("Cannot extract TAR.GZ archive")
        end
      other ->
        IO.inspect("Archive with #{other} extension is unsupported")
    end
  end
end


defmodule Receive do
  def handle_parser_result() do
    receive do
      {:basic_deliver, parsed_content, _meta} ->
        IO.inspect("Received parsed content : #{parsed_content}")

        parsed_content
    end
  end

  def handle_extract_requests(channel) do
    receive do
      {:basic_deliver, payload, meta} ->
        paths = Extract.extract(payload)
        base_location = "../../../storage/"

        parsed_contents = Enum.map(paths, fn full_path ->
          short_path = String.replace(to_string(full_path), base_location, "")
          extension = List.last(String.split(short_path, "."))

          {:ok, %{queue: queue_name}} = AMQP.Queue.declare(channel,
            "",
            exclusive: true)
          AMQP.Basic.consume(channel, queue_name, nil, no_ack: true)

          # request for parsing every file in archive
          AMQP.Basic.publish(channel,
            "",
            extension,
            short_path,
            reply_to: queue_name)

          content = handle_parser_result()
          content
        end)

        # send to backend concatenation of contents of every file to index archive
        AMQP.Basic.publish(channel,
          "",
          meta.reply_to,
          "#{parsed_contents}")

        handle_extract_requests(channel)
    end
  end
end

host = if length(System.argv) > 0 do
    Enum.at(System.argv, 0)
else
    "localhost"
end
port = "5672"
url = "amqp://guest:guest@" <> host <> ":" <> port
{:ok, connection} = AMQP.Connection.open(url, heartbeat: 600, timeout: 1000)
{:ok, channel_extract} = AMQP.Channel.open(connection)
IO.puts("Connection with RabbitMQ opened with host: #{host}")

AMQP.Queue.declare(channel_extract, "zip")
AMQP.Queue.declare(channel_extract, "tar")
AMQP.Queue.declare(channel_extract, "gz")
AMQP.Basic.consume(channel_extract, "zip", nil, no_ack: true)
AMQP.Basic.consume(channel_extract, "tar", nil, no_ack: true)
AMQP.Basic.consume(channel_extract, "gz", nil, no_ack: true)
IO.puts("Waiting for messages...")

Receive.handle_extract_requests(channel_extract)
