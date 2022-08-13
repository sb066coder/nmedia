package ru.netology.nmedia.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.model.Post
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.util.AndroidUtils

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Создание объекта binding с наполнением из activity_main
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Создание объекта PostViewModel
        val viewModel: PostViewModel by viewModels()

        // Создание контракта
        val editPostActivity = registerForActivityResult(EditPostResultContract()) { result ->
            if(result == null) {
                viewModel.cancel()
                return@registerForActivityResult
            }
            viewModel.changeContent(result)
            viewModel.save()
        }

        // Создание адаптера
        val adapter = PostsAdapter (
            object : OnInteractionListener {
                override fun onLike(post: Post) { viewModel.likeById(post.id) }
                override fun onRemove(post: Post) { viewModel.deleteById(post.id) }
                override fun onShare(post: Post) {
                    viewModel.shareById(post.id)
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(intent, getString(R.string.chooser_share_post))
                    startActivity(shareIntent)
                }
                override fun onEdit(post: Post) {
                    viewModel.edit(post)
                    editPostActivity.launch(post.content)
                }

                override fun onViewVideo(post: Post) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.videoContent))
                    startActivity(intent)
                }
            }
        )

        binding.rvList.adapter = adapter

        viewModel.data.observe(this) { posts ->
            adapter.submitList(posts) //Адаптер принимает новый список posts и сравнивает с имеющимся для внесения изменений
        }

        binding.fabAdd.setOnClickListener {
            editPostActivity.launch("")
        }
    }
}